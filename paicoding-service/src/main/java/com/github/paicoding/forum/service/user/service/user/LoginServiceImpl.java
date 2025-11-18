package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.api.model.vo.user.UserSaveReq;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.UserService;
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
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserPwdEncoder userPwdEncoder;

    @Autowired
    private UserService userService;

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
     * @param userId 用户id
     * @return
     */
    @Override
    public String loginByWx(Long userId) {
        return userSessionHelper.genToken(userId);
    }

    /**
     * 用户名密码方式登录
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @Override
    public String loginByUserPwd(String username, String password) {
        UserDO user = userDao.getUserByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + username);
        }

        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        Long userId = user.getId();
        // 登录成功，返回对应的session
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genToken(userId);
    }


    /**
     * 用户名密码方式登录，若用户不存在，则进行注册
     *
     * @param loginReq 登录信息
     * @return
     */
    @Override
    public String registerByUserPwd(UserPwdLoginReq loginReq) {
        // 1. 前置校验
        registerPreCheck(loginReq);

        // 2. 判断当前用户是否登录，若已经登录，则直接走绑定流程
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        loginReq.setUserId(userId);
        if (userId != null) {
            // 2.1 如果用户已经登录，则走绑定用户信息流程
            userService.bindUserInfo(loginReq);
            return ReqInfoContext.getReqInfo().getSession();
        }


        // 3. 尝试使用用户名进行登录，若成功，则依然走绑定流程
        UserDO user = userDao.getUserByUserName(loginReq.getUsername());
        if (user != null) {
            if (!userPwdEncoder.match(loginReq.getPassword(), user.getPassword())) {
                // 3.1 用户名已经存在
                throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, loginReq.getUsername());
            }

            // 3.2 用户存在，密码正确，直接登录
            userId = user.getId();
            loginReq.setUserId(userId);
        } else {
            //4. 走用户注册流程
            userId = registerService.registerByUserNameAndPassword(loginReq);
        }
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genToken(userId);
    }


    /**
     * 注册前置校验
     *
     * @param loginReq
     */
    private void registerPreCheck(UserPwdLoginReq loginReq) {
        if (StringUtils.isBlank(loginReq.getUsername()) || StringUtils.isBlank(loginReq.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }
    }
}
