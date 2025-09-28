package com.github.paicoding.forum.api.model.enums.login;

import lombok.Getter;

/**
 * 微信公众号登录二维码类型
 *
 * @author YiHui
 * @date 2025/9/28
 */
@Getter
public enum LoginQrTypeEnum {

    SUBSCRIPTION_ACCOUNT("Subscription Account", "微信公众号"),
    SERVICE_ACCOUNT("Service Account", "服务号"),
    ;
    private String code;
    private String desc;

    LoginQrTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
