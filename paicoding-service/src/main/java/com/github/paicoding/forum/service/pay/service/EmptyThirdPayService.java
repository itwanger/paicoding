
package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.service.pay.ThirdPayService;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

/**
 * 三方支付服务
 *
 * @author YiHui
 * @date 2024/12/3
 */
@Service
@ConditionalOnMissingBean(ThirdPayService.class)
public class EmptyThirdPayService {

    /**
     * 下单
     *
     * @param payReq
     * @param payWay
     * @return
     */
    public PrePayInfoResBo createPayOrder(ThirdPayOrderReqBo payReq, ThirdPayWayEnum payWay) {
        return null;
    }

    /**
     * 查询订单，根据商户订单号
     *
     * @return
     */
    public Transaction queryPayOrderOutTradeNo(String transNo, ThirdPayWayEnum payWay) {
        return null;
    }

    /**
     * 关闭订单
     *
     * @return
     */
    public void closePayOrder(String outTradeNo, ThirdPayWayEnum payWay) {

    }

    /**
     * 构建前端唤醒支付的相关信息
     *
     * @param prePayId
     * @param outTradeNo
     * @param payWay
     * @return
     * @throws Exception
     */
    public PrePayInfoResBo genToPayPrePayInfo(String prePayId, String outTradeNo, ThirdPayWayEnum payWay) {
        return null;
    }

    /**
     * 支付回调
     *
     * @param request     携带回传的请求参数
     * @param payCallback 支付结果回调执行业务逻辑
     * @return
     * @throws IOException
     */
    public ResponseEntity payCallback(HttpServletRequest request, Function<Transaction, Boolean> payCallback) throws IOException {
        return null;
    }


    /**
     * 退款回调
     *
     * @param request        携带回传的请求参数
     * @param refundCallback 退款结果回调执行业务逻辑
     * @return
     * @throws IOException
     */
    public ResponseEntity refundCallback(HttpServletRequest request, Function<RefundNotification, Boolean> refundCallback) throws IOException {
        return null;
    }
}
