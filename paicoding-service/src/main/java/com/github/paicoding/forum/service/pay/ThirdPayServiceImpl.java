package com.github.paicoding.forum.service.pay;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.ThirdPayIntegrationApi;
import com.github.paicoding.forum.service.pay.service.email.EmailPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * 三方支付集成服务
 *
 * @author YiHui
 * @date 2024/12/6
 */
@Service
public class ThirdPayServiceImpl implements ThirdPayService {
    @Autowired
    private List<ThirdPayIntegrationApi> payServiceList;

    public ThirdPayIntegrationApi getPayService(ThirdPayWayEnum payWay) {
        return payServiceList.stream().filter(s -> s.support(payWay)).findFirst()
                .orElse(SpringUtil.getBean(EmailPayService.class));
    }

    @Override
    public PrePayInfoResBo createPayOrder(ThirdPayOrderReqBo payReq) {
        return getPayService(payReq.getPayWay()).createOrder(payReq);
    }

    @Override
    public ResponseEntity<?> payCallback(HttpServletRequest request, ThirdPayWayEnum payWay, Function<PayCallbackBo, Boolean> payCallback) throws IOException {
        return getPayService(payWay).payCallback(request, payCallback);
    }

    @Override
    public <T> ResponseEntity<?> refundCallback(HttpServletRequest request, ThirdPayWayEnum payWay, Function<T, Boolean> refundCallback) throws IOException {
        return getPayService(payWay).refundCallback(request, refundCallback);
    }
}
