package com.github.paicoding.forum.service.pay.model;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import lombok.Data;

/**
 * @author YiHui
 * @date 2024/12/3
 */
@Data
public class PrePayInfoResBo {
    /**
     * 支付方式 wx-jsapi, wx-h5, wx-native
     *
     * @see ThirdPayWayEnum#getPay()
     */
    private String payWay;

    /**
     * 传递给三方的外部系统编号
     */
    private String outTradeNo;

    /**
     * 应用: appId
     */
    private String appId;

    /**
     * 时间戳信息
     */
    private String nonceStr;

    private String prePackage;

    private String paySign;

    private String timeStamp;

    private String signType;

    /**
     * jsapi：返回的是用于唤起支付的 id
     * h5: 返回的是微信收银台中间页 url
     */
    private String prePayId;

    /**
     * 失效的时间戳
     */
    private Long expireTime;
}
