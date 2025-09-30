package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserTransferService;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
@Slf4j
public class UserTransferServiceImpl implements UserTransferService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private UserPwdEncoder userPwdEncoder;


    @Override
    public boolean transferUser(String uname, String pwd) {
        UserDO user = userDao.getUserByUserName(uname);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + uname);
        }

        if (!userPwdEncoder.match(pwd, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        // 将当前登录用户，与目标用户进行置换
        return transferUser(user);
    }

    @Override
    public boolean transferUser(String starNumber) {
        // 根据星球号找到原始用户
        UserAiDO userAiDO = userAiDao.getByStarNumber(starNumber);
        if (userAiDO == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "starNumber=" + starNumber);
        }

        // 找到需要迁移到的目标用户
        UserDO targetUser = userDao.getUserByUserId(userAiDO.getUserId());
        return transferUser(targetUser);
    }

    /**
     * 账号迁移
     *
     * @param targetUser
     */
    private boolean transferUser(UserDO targetUser) {
        Long currentUserId = ReqInfoContext.getReqInfo().getUserId();
        if (Objects.equals(currentUserId, targetUser.getId())) {
            // 同一个用户，无需迁移
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "当前用户与目标用户相同，无需迁移");
        }

        UserDO loginUser = userDao.getUserByUserId(currentUserId);
        if (StringUtils.isBlank(loginUser.getThirdAccountId())) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "非企业号登录，无需账号迁移");
        }

        String oldId = targetUser.getThirdAccountId();
        String transId = loginUser.getThirdAccountId();

        // 将当前登录用户的微信身份信息，转移到目标用户
        targetUser.setThirdAccountId(transId);
        userDao.updateUser(targetUser);

        // 将目标用户的微信身份信息，转移到当前登录用户
        loginUser.setThirdAccountId(oldId);
        userDao.updateUser(loginUser);

        // 迁移成功，重置上下文信息
        ReqInfoContext.getReqInfo().setUserId(targetUser.getId());
        String session = userSessionHelper.genSession(targetUser.getId());
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        if (response != null) {
            response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
        }
        log.info("用户迁移成功，从 {} -> {}", loginUser.getId(), targetUser.getId());
        return true;
    }
}
