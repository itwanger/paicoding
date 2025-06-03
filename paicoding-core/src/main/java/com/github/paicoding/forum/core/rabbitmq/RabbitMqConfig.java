package com.github.paicoding.forum.core.rabbitmq;

import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.core.config.RabbitmqProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: pai_coding
 * @description: rabbitmq的自定义消息转换器
 * @author: XuYifei
 * @create: 2024-11-01
 */
@ConditionalOnProperty(value = "rabbitmq.switchFlag")
@EnableConfigurationProperties(RabbitmqProperties.class)
@Configuration
public class RabbitMqConfig {
    @Bean
    public MessageConverter messageConverter(){
        // 1.定义消息转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        // 2.配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(CommonConstants.MESSAGE_QUEUE_EXCHANGE_NAME_DIRECT);
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(CommonConstants.MESSAGE_QUEUE_EXCHANGE_NAME_FANOUT);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(CommonConstants.MESSAGE_QUEUE_EXCHANGE_NAME_TOPIC);
    }


    @Bean
    public Queue notifyQueue(){
        return new Queue(CommonConstants.MESSAGE_QUEUE_NAME_NOTIFY_EVENT, true);
    }

    @Bean
    public Binding bindingPraise(Queue notifyQueue, DirectExchange directExchange){
        return BindingBuilder.bind(notifyQueue).to(directExchange).with(CommonConstants.MESSAGE_QUEUE_KEY_NOTIFY);
    }
}
