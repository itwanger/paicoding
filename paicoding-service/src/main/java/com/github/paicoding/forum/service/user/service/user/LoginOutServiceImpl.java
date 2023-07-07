package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
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

import java.util.Objects;

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
    public String autoRegisterAndGetVerifyCode(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        req.setUserId(registerOrGetUserInfo(req));
        return userSessionHelper.genVerifyCode(req.getUserId());
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
    public String register(String code) {
        Long userId = userSessionHelper.getUserIdByCode(code);
        if (userId == null) {
            return null;
        }
        return userSessionHelper.codeVerifySucceed(code, userId);
    }

    @Override
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }


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

        UserDO user = userDao.getUserByUserName(userName);
        Long userId;
        if (user == null) {
            // 用户不存在，走注册流程
            userId = registerNewUser(userName, password, starNumber, invitationCode);
        } else if (userPwdEncoder.match(password, user.getPassword())) {
            // 走登录绑定流程
            userId = bindUserAccount(user.getId(), starNumber, invitationCode);
        } else {
            // 用户名密码不匹配
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, userName);
        }

        return userSessionHelper.genSession(userId);
    }

    private Long registerNewUser(String userName, String password, String starNumber, String invitationCode) {
        if (StringUtils.isNotBlank(starNumber) && userAiDao.getByInviteCode(starNumber) != null) {
            // 判断星球是否已经被绑定了
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, starNumber);
        }

        // 注册用户
        return registerService.registerByUserNameAndPassword(userName, password, starNumber, invitationCode);
    }

    private Long bindUserAccount(Long userId, String starNumber, String invitationCode) {
        // 根据星球编号直接查询用户是否存在，存在则直接登录，不存在则进行注册
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        if (userAiDO == null) {
            userAiDO = UserAiConverter.initAi(userId);
        }
        if (!Objects.equals(starNumber, userAiDO.getStarNumber())) {
            // 不同时，更新星球号，并设置为试用
            userAiDO.setStarNumber(starNumber).setState(UserAIStatEnum.TRYING.getCode());
        }
        userAiDao.saveOrUpdateAiBindInfo(userAiDO, invitationCode);
        return userId;
    }

    private Long bindUserAccount(Long userId) {
        // 根据星球编号直接查询用户是否存在，存在则直接登录，不存在则进行注册
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        if (userAiDO == null) {
            userAiDO = UserAiConverter.initAi(userId);
        }
        userAiDao.saveOrUpdateAiBindInfo(userAiDO);
        return userId;
    }
}
