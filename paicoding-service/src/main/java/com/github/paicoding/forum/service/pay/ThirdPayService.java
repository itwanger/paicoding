package com.github.paicoding.forum.service.pay;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
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
    default ThirdPayWayEnum getPayWay() {
        return ThirdPayWayEnum.EMAIL;
    }

    /**
     * 下单
     *
     * @param payReq
     * @return
     * @throws Exception
     */
    PrePayInfoResBo createPayOrder(ThirdPayOrderReqBo payReq);

    /**
     * 支付回调
     *
     * @param request     携带回传的请求参数
     * @param payCallback 支付结果回调执行业务逻辑
     * @return
     * @throws IOException
     */
    ResponseEntity payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) throws IOException;


    /**
     * 退款回调
     *
     * @param request        携带回传的请求参数
     * @param refundCallback 退款结果回调执行业务逻辑
     * @return
     * @throws IOException
     */
    <T> ResponseEntity refundCallback(HttpServletRequest request, Function<T, Boolean> refundCallback) throws IOException;
}
