package com.github.paicoding.forum.test.rabbitmq;

import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.notify.service.RabbitmqService;
import com.github.paicoding.forum.test.BasicTest;
import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqTest extends BasicTest {

    @Autowired
    private RabbitmqService rabbitmqService;

    @Test
    public void testProductRabbitmq() {
        try {
            rabbitmqService.publishMsg(
                    CommonConstants.EXCHANGE_NAME_DIRECT,
                    BuiltinExchangeType.DIRECT,
                    CommonConstants.QUERE_KEY_PRAISE,
                    "lvmenglou test msg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConsumerRabbitmq() {
        try {
            rabbitmqService.consumerMsg(CommonConstants.EXCHANGE_NAME_DIRECT, CommonConstants.QUERE_KEY_PRAISE, CommonConstants.QUERE_KEY_PRAISE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
