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


    // fixme 对于支付状态为支付中的场景，根据notify_time来判断，是否需要重新给作者发送邮件通知 / 或者查询微信的订单状态，避免出现支付状态一直不更新的问题
    public void autoUpdatePayingStatus() {
        // 1. 查出 支付状态 == 支付中， notifyTime = null 或者 notifyTime 距离当前时间超过30分钟的数据
        // 2. 重新调用一下 payService.paying 实现给作者发邮件 or 查三方支付状态
        // 3. 更新校验时间
    }
}
