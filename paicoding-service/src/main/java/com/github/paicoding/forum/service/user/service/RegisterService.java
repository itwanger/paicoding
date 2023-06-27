package com.github.paicoding.forum.service.user.service;

/**
 * 用户注册服务
 *
 * @author YiHui
 * @date 2023/6/26
 */
public interface RegisterService {
    /**
     * 通过用户名/密码进行注册
     *
     * @param username
     * @param star
     * @param password
     * @return
     */
    Long registerByUserNameAndPassword(String username, String password, String star, String inviteCode);

    /**
     * 通过微信公众号进行注册
     *
     * @param thirdAccount
     * @return
     */
    Long registerByWechat(String thirdAccount);
}
