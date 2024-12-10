
package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.integration.ThirdPayIntegrationApi;
import com.github.paicoding.forum.service.pay.service.integration.email.EmailPayIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Function;

/**
 * 与三方支付服务交互的门面类
 *
 * @author YiHui
 * @date 2024/12/6
 */
@Service
public class ThirdPayHandler {
    @Autowired
    private List<ThirdPayIntegrationApi> payServiceList;

    private ThirdPayIntegrationApi getPayService(ThirdPayWayEnum payWay) {
        return payServiceList.stream().filter(s -> s.support(payWay)).findFirst()
                .orElse(SpringUtil.getBean(EmailPayIntegration.class));
    }

    public PrePayInfoResBo createPayOrder(ThirdPayOrderReqBo payReq) {
        return getPayService(payReq.getPayWay()).createOrder(payReq);
    }

    public PayCallbackBo queryOrder(String outTradeNo, ThirdPayWayEnum payWay) {
        return getPayService(payWay).queryOrder(outTradeNo);
    }

    @Transactional
    public PayCallbackBo payCallback(HttpServletRequest request, ThirdPayWayEnum payWay) {
        return getPayService(payWay).payCallback(request);
    }

    @Transactional
    public <T> ResponseEntity<?> refundCallback(HttpServletRequest request, ThirdPayWayEnum payWay, Function<T, Boolean> refundCallback) {
        return getPayService(payWay).refundCallback(request, refundCallback);
    }
}
