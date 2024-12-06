package com.github.paicoding.forum.service.pay.model;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YiHui
 * @date 2024/12/3
 */
@Data
@Accessors(chain = true)
public class PrePayInfoResBo {
    /**
     * 支付方式 wx-jsapi, wx-h5, wx-native
     *
     * @see ThirdPayWayEnum#getPay()
     */
    private ThirdPayWayEnum payWay;

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
     * jsapi：返回的是用于唤起支付的 prePayId
     * h5: 返回的是微信收银台中间页 url，用于访问之后唤起微信客户端的支付页面
     * native: 返回的是形如 weixin:// 的文本，用于生成二维码给微信扫一扫支付
     */
    private String prePayId;

    /**
     * prePayId的失效的时间戳
     */
    private Long expireTime;
}
