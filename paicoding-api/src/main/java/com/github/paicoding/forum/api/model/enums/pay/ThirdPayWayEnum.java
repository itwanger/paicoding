package com.github.paicoding.forum.api.model.enums.pay;

import lombok.Getter;

/**
 * 三方平台支付方式
 *
 * @author YiHui
 * @date 2024/12/3
 */
public enum ThirdPayWayEnum {
    WX_H5("wx_h5"),
    WX_JSAPI("wx_jsapi"),
    WX_NATIVE("wx_native"),
    ;

    @Getter
    private String pay;

    ThirdPayWayEnum(String pay) {
        this.pay = pay;
    }
}
