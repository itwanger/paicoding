package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface LoginService {
    String SESSION_KEY = "f-session";
    String USER_DEVICE_KEY = "f-device";


    /**
     * 适用于微信公众号登录场景下，自动注册一个用户
     *
     * @param uuid 微信唯一标识
     * @return userId 用户主键
     */
    Long autoRegisterWxUserInfo(String uuid);

    /**
     * 登出
     *
     * @param session 用户会话
     */
    void logout(String session);

    /**
     * 给微信公众号的用户生成一个用于登录的会话
     *
     * @param userId 用户主键id
     * @return
     */
    String loginByWx(Long userId);

    /**
     * 用户名密码方式登录
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    String loginByUserPwd(String username, String password);

    /**
     * 注册登录，并绑定对应的星球、邀请码
     *
     * @param loginReq 登录信息
     * @return
     */
    String registerByUserPwd(UserPwdLoginReq loginReq);
}
