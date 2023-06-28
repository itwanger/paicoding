package com.github.paicoding.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YiHui
 * @date 2023/6/26
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    /**
     * 微信登录
     */
    WECHAT(0),
    /**
     * 用户名+密码登录
     */
    USER_PWD(1),
    ;
    private int type;
}
