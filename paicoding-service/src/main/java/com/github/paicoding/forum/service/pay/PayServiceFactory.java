package com.github.paicoding.forum.service.pay;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技术派的支付服务接口
 *
 * @author YiHui
 * @date 2024/12/9
 */
@Service
public class PayServiceFactory {

    @Autowired
    private List<PayService> payServiceList;

    public PayService getPayService(ThirdPayWayEnum payWay) {
        for (PayService payService : payServiceList) {
            if (payService.support(payWay)) {
                return payService;
            }
        }

        return null;
    }
}
