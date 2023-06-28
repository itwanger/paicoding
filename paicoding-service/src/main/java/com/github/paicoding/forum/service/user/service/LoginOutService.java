package com.github.paicoding.forum.service.user.service;

/**
 * @author YiHui
 * @date 2022/8/15
 */
public interface LoginOutService {
    String SESSION_KEY = "f-session";
    String USER_DEVICE_KEY = "f-device";


    /**
     * 适用于微信公众号登录场景下，自动注册一个用户，并返回登录验证码
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
    String register(String code);


    /**
     * 登出
     *
     * @param session
     */
    void logout(String session);

    /**
     * 用户名密码方式登录
     *
     * @param username
     * @param password
     * @return
     */
    String register(String username, String password);

    /**
     * 注册登录，并绑定对应的星球、邀请码
     *
     * @param userName
     * @param password
     * @param starNumber
     * @param invitationCode
     * @return
     */
    String register(String userName, String password, String starNumber, String invitationCode);
}
