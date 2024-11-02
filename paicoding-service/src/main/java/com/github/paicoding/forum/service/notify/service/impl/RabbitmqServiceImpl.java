package com.github.paicoding.forum.service.notify.service.impl;

import com.github.paicoding.forum.api.model.event.MessageQueueEvent;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.notify.service.RabbitmqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitmqServiceImpl implements RabbitmqService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FanoutExchange fanoutExchange;

    @Autowired
    private DirectExchange directExchange;

    @Autowired
    private TopicExchange topicExchange;


    @Override
    public boolean enabled() {
        return "true".equalsIgnoreCase(SpringUtil.getConfig("rabbitmq.switchFlag"));
    }

//    @Override
//    public void publishMsg(String exchange,
//                           BuiltinExchangeType exchangeType,
//                           String toutingKey,
//                           String message) {
//        try {
//            //创建连接
//            RabbitmqConnection rabbitmqConnection = RabbitmqConnectionPool.getConnection();
//            Connection connection = rabbitmqConnection.getConnection();
//            //创建消息通道
//            Channel channel = connection.createChannel();
//            // 声明exchange中的消息为可持久化，不自动删除
//            channel.exchangeDeclare(exchange, exchangeType, true, false, null);
//            // 发布消息
//            channel.basicPublish(exchange, toutingKey, null, message.getBytes());
//            log.info("Publish msg: {}", message);
//            channel.close();
//            RabbitmqConnectionPool.returnConnection(rabbitmqConnection);
//        } catch (InterruptedException | IOException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent, String key) {
        this.publishDirectMsg(messageQueueEvent, key, true);
    }

    public <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent, String key, boolean isPersist) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryMode(isPersist? MessageDeliveryMode.PERSISTENT: MessageDeliveryMode.NON_PERSISTENT);

        Message message = rabbitTemplate.getMessageConverter().toMessage(messageQueueEvent, messageProperties);
        rabbitTemplate.convertAndSend(directExchange.getName(), key, message);
    }

    @Override
    public <T> void publishFanoutMsg(MessageQueueEvent<T> messageQueueEvent) {
        rabbitTemplate.convertAndSend(fanoutExchange.getName(), messageQueueEvent);
    }

    @Override
    public <T> void publishTopicMsg(MessageQueueEvent<T> messageQueueEvent, String key) {
        rabbitTemplate.convertAndSend(topicExchange.getName(), key, messageQueueEvent);
    }


}
