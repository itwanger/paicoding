package com.github.paicoding.forum.service.notify.config;

import com.github.paicoding.forum.core.config.RabbitmqProperties;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 当未开启rabbitMq时，提供一套默认的注入bean，避免 RabbitMqService 启动失败
 * @author XuYifei
 * @date 2024-07-12
 */
@Configuration
@ConditionalOnProperty(value = "rabbitmq.switchFlag", havingValue = "false")
@EnableConfigurationProperties(RabbitmqProperties.class)
public class RabbitMqAutoConfig implements ApplicationRunner {
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("");
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("");
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }
}
