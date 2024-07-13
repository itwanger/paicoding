package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;

/**
 * 用户注册服务
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface RegisterService {
    /**
     * 通过用户名/密码进行注册
     *
     * @param loginReq
     * @return
     */
    Long registerByUserNameAndPassword(UserPwdLoginReq loginReq);

    /**
     * 通过微信公众号进行注册
     *
     * @param thirdAccount
     * @return
     */
    Long registerByWechat(String thirdAccount);
}
