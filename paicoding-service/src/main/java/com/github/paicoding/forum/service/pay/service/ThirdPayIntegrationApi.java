package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

/**
 * 对接三方支付的API定义
 *
 * @author YiHui
 * @date 2024/12/6
 */
public interface ThirdPayIntegrationApi {

    boolean support(ThirdPayWayEnum payWay);

    /**
     * 微信下单
     *
     * @param payReq
     * @return
     */
    PrePayInfoResBo createOrder(ThirdPayOrderReqBo payReq);

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
    PayCallbackBo queryOrder(String outTradeNo);


    /**
     * 支付结果回调
     *
     * @param request
     * @return
     * @throws IOException
     */
    ResponseEntity<?> payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) throws IOException;


    /**
     * 退款回调
     *
     * @param request        携带回传的请求参数
     * @param refundCallback 退款结果回调执行业务逻辑
     * @return
     * @throws IOException
     */
    <T> ResponseEntity<?> refundCallback(HttpServletRequest request,  Function<T, Boolean> refundCallback) throws IOException;
}
