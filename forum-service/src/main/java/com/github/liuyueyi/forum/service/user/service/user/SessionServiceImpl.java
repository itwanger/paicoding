package com.github.liuyueyi.forum.service.user.service.user;

import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.service.user.service.SessionService;
import com.github.liuyueyi.forum.service.user.service.UserService;
import com.github.liuyueyi.forum.service.user.service.help.UserSessionHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Override
    public String getVerifyCode(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        userService.registerOrGetUserInfo(req);
        return userSessionHelper.genVerifyCode(req.getUserId());
    }

    @Override
    public String login(String code) {
        Long userId = userSessionHelper.getUserIdByCode(code);
        if (userId == null) {
            return null;
        }
        return userSessionHelper.codeVerifySucceed(code, userId);
    }

    @Override
    public String login(Long userId) {
        return userSessionHelper.codeVerifySucceed("", userId);
    }

    @Override
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }


    @Override
    public BaseUserInfoDTO getUserBySessionId(String session) {
        if (StringUtils.isBlank(session)) {
            return null;
        }

        Long userId = userSessionHelper.getUserIdBySession(session);
        return userId == null ? null : userService.queryBasicUserInfo(userId);
    }
}
