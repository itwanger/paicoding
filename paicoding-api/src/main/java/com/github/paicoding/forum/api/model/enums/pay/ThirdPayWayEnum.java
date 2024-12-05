package com.github.paicoding.forum.api.model.enums.pay;

import lombok.Getter;

import java.util.Objects;

/**
 * 三方平台支付方式
 *
 * @author YiHui
 * @date 2024/12/3
 */
public enum ThirdPayWayEnum {
    WX_H5("wx_h5", "H5") {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    WX_JSAPI("wx_jsapi", "JS") {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    WX_NATIVE("wx_native", "NA") {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    /**
     * 个人收款码，基于邮件进行确认的模式
     */
    EMAIL("email", "EM"),
    ;

    @Getter
    private String pay;

    /**
     * 外部支付编号的前缀
     */
    @Getter
    private String prefix;

    ThirdPayWayEnum(String pay, String prefix) {
        this.pay = pay;
        this.prefix = prefix;
    }

    public static ThirdPayWayEnum ofPay(String pay) {
        for (ThirdPayWayEnum value : values()) {
            if (Objects.equals(value.pay, pay)) {
                return value;
            }
        }
        return null;
    }

    public boolean wxPay() {
        return false;
    }
}
