package com.github.paicoding.forum.service.pay;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

/**
 * 三方支付服务
 *
 * @author YiHui
 * @date 2024/12/3
 */
public interface ThirdPayService {
    /**
     * JSAPI下单
     *
     * @return
     */
    PrePayInfoResBo jsApiOrder(ThirdPayOrderReqBo payReq) throws Exception;

    /**
     * h5 支付
     *
     * @param payReq
     * @return
     * @throws Exception
     */
    PrePayInfoResBo h5ApiOrder(ThirdPayOrderReqBo payReq) throws Exception;

    /**
     * 构建前端唤醒支付的相关信息
     *
     * @param prePayId
     * @param outTradeNo
     * @param payWay
     * @return
     * @throws Exception
     */
    PrePayInfoResBo genToPayPrePayInfo(String prePayId, String outTradeNo, ThirdPayWayEnum payWay);

    /**
     * 支付回调
     *
     * @param request
     * @return
     * @throws IOException
     */
    ResponseEntity callback(HttpServletRequest request, Function<Transaction, Boolean> payCallback) throws IOException;

    /**
     * 查询订单，根据商户订单号
     *
     * @return
     */
    Transaction queryWxPayOrderOutTradeNo(String transNo);

    /**
     * 关闭订单
     *
     * @return
     */
    void closePay(String outTradeNo);

    ResponseEntity refundCallback(HttpServletRequest request, Function<RefundNotification, Boolean> refundCallback) throws IOException;
}
