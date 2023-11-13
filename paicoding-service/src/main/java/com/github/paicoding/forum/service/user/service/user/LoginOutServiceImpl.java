package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserSaveReq;
import com.github.paicoding.forum.service.user.converter.UserAiConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginOutService;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.help.StarNumberHelper;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
@Slf4j
public class LoginOutServiceImpl implements LoginOutService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;


    @Autowired
    private UserSessionHelper userSessionHelper;
    @Autowired
    private StarNumberHelper starNumberHelper;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserPwdEncoder userPwdEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoRegisterWxUserInfo(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        Long userId = registerOrGetUserInfo(req);
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userId;
    }

    /**
     * 没有注册时，先注册一个用户；若已经有，则登录
     *
     * @param req
     */
    private Long registerOrGetUserInfo(UserSaveReq req) {
        UserDO user = userDao.getByThirdAccountId(req.getThirdAccountId());
        if (user == null) {
            return registerService.registerByWechat(req.getThirdAccountId());
        }
        return user.getId();
    }

    @Override
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }

    /**
     * 给微信公众号的用户生成一个用于登录的会话
     *
     * @param userId
     * @return
     */
    @Override
    public String register(Long userId) {
        return userSessionHelper.genSession(userId);
    }

    /**
     * 用户名密码方式登录注册
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public String register(String username, String password) {
        UserDO user = userDao.getUserByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + username);
        }

        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        Long userId = bindUserAccount(user.getId());

        // 登录成功，返回对应的session
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genSession(userId);
    }


    @Override
    public String register(String userName, String password, String starNumber, String invitationCode) {
        // 若传了星球信息，首先进行校验
        if (starNumber != null && Boolean.FALSE.equals(starNumberHelper.checkStarNumber(starNumber))) {
            // 星球编号校验不通过，直接抛异常
            throw ExceptionUtil.of(StatusEnum.USER_STAR_NOT_EXISTS, "星球编号=" + starNumber);
        }

        if (StringUtils.isNotBlank(starNumber) && userAiDao.getByInviteCode(starNumber) != null) {
            // 判断星球是否已经被绑定了
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, starNumber);
        }

        Long userId = ReqInfoContext.getReqInfo().getUserId();

        // 如果用户已经登录，则是一个绑定操作
        if (userId != null) {
            // 走绑定流程
            registerService.bindOldUser(userName, password, starNumber, invitationCode, userId);
        } else {
            // 走注册流程
            userId = registerService.registerByUserNameAndPassword(userName, password, starNumber, invitationCode);
        }

        return userSessionHelper.genSession(userId);
    }

    private Long bindUserAccount(Long userId) {
        // 用户名密码注册的时候初始化用户AI信息
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        if (userAiDO == null) {
            userAiDO = UserAiConverter.initAi(userId);
        }
        userAiDao.saveOrUpdateAiBindInfo(userAiDO);
        return userId;
    }
}
