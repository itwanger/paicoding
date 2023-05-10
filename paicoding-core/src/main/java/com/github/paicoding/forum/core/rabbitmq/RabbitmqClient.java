package com.github.paicoding.forum.core.rabbitmq;

import com.github.paicoding.forum.core.config.RabbitmqProperties;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Louzai
 * @date 2023/5/10
 */
@Component
public class RabbitmqClient {

    @Autowired
    private RabbitmqProperties rabbitmqProperties;

    /**
     * 创建一个工厂
     * @param key
     * @return
     */
    public ConnectionFactory getConnectionFactory(String key) {
        String host = rabbitmqProperties.getHost();
        Integer port = rabbitmqProperties.getPort();
        String userName = rabbitmqProperties.getUsername();
        String password = rabbitmqProperties.getPassport();
        String virtualhost = rabbitmqProperties.getVirtualhost();
        return RabbitmqUtil.getOrInitConnectionFactory(key, host, port, userName,password, virtualhost);
    }
}
