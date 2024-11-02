package com.github.paicoding.forum.service.notify.config;

import com.github.paicoding.forum.core.config.RabbitmqProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
//@Configuration
@ConditionalOnProperty(value = "rabbitmq.switchFlag")
@EnableConfigurationProperties(RabbitmqProperties.class)
public class RabbitMqAutoConfig implements ApplicationRunner {
//    @Resource
//    private RabbitmqService rabbitmqService;

    @Autowired
    private RabbitmqProperties rabbitmqProperties;


    @Override
    public void run(ApplicationArguments args) throws Exception {
//        String host = rabbitmqProperties.getHost();
//        Integer port = rabbitmqProperties.getPort();
//        String userName = rabbitmqProperties.getUsername();
//        String password = rabbitmqProperties.getPassport();
//        String virtualhost = rabbitmqProperties.getVirtualhost();
//        Integer poolSize = rabbitmqProperties.getPoolSize();
//        RabbitmqConnectionPool.initRabbitmqConnectionPool(host, port, userName, password, virtualhost, poolSize);
//        AsyncUtil.execute(() -> rabbitmqService.processConsumerMsg());
    }
}
