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
    // // 官方说明有效期五分钟，我们这里设置一下有效期为四分之后，避免正好卡在失效的时间点
    WX_H5("wx_h5", "H5", 250_000) {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    // 官方说明有效期为两小时，我们设置为1.8小时之后失效
    WX_JSAPI("wx_jsapi", "JS", 18 * 360_000) {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    // 官方说明有效期为两小时，我们设置为1.8小时之后失效
    WX_NATIVE("wx_native", "NA", 18 * 360_000) {
        @Override
        public boolean wxPay() {
            return true;
        }
    },
    /**
     * 个人收款码，基于邮件进行确认的模式，设置30天的有效期
     */
    EMAIL("email", "EM", 30 * 3600_000),
    ;

    @Getter
    private String pay;

    /**
     * 外部支付编号的前缀
     */
    @Getter
    private String prefix;

    /**
     * prePay有效时间间隔，单位毫秒
     */
    @Getter
    private Integer expireTimePeriod;

    ThirdPayWayEnum(String pay, String prefix, Integer expireTimePeriod) {
        this.pay = pay;
        this.prefix = prefix;
        this.expireTimePeriod = expireTimePeriod;
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
