package com.github.paicoding.forum.service.pay.service.wx;

import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.service.payments.model.Transaction;

/**
 * 微信支付整合接口
 *
 * @author YiHui
 * @date 2024/12/6
 */
public interface WxPayIntegrationApi {

    /**
     * 微信下单
     *
     * @param payReq
     * @return
     */
    String createOrder(ThirdPayOrderReqBo payReq);

    /**
     * 关单
     *
     * @param outTradeNo
     */
    void closeOrder(String outTradeNo);

    /**
     * 查询订单
     *
     * @param outTradeNo
     * @return
     */
    Transaction queryOrder(String outTradeNo);
}
