package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author YiHui
 * @date 2022/8/15
 */
public interface SessionService {
    String SESSION_KEY = "f-session";
    String USER_DEVICE_KEY = "f-device";
    Set<String> LOGIN_CODE_KEY = Sets.newHashSet("登录", "login");


    /**
     * 获取登录验证码
     *
     * @param uuid
     * @return
     */
    String autoRegisterAndGetVerifyCode(String uuid);

    /**
     * 登录
     *
     * @param code
     * @return
     */
    String login(String code);

    /**
     * 用户名密码方式，直接登录获取session
     *
     * @param userId
     * @return
     */
    String login(Long userId);

    /**
     * 登出
     *
     * @param session
     */
    void logout(String session);


    /**
     * 获取登录的用户信息,并更行丢对应的ip信息
     *
     * @param session
     * @param clientIp
     * @return
     */
    BaseUserInfoDTO getAndUpdateUserIpInfoBySessionId(String session, String clientIp);
}
