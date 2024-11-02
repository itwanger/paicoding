package com.github.paicoding.forum.service.notify.service;

import com.github.paicoding.forum.api.model.event.MessageQueueEvent;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface RabbitmqService {

    boolean enabled();

//    /**
//     * 发布消息
//     *
//     * @param exchange
//     * @param exchangeType
//     * @param toutingKey
//     * @param message
//     * @throws IOException
//     * @throws TimeoutException
//     */
//    void publishMsg(String exchange,
//                    BuiltinExchangeType exchangeType,
//                    String toutingKey,
//                    String message) throws IOException, TimeoutException;

    <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent, String key);

    <T> void publishFanoutMsg(MessageQueueEvent<T> messageQueueEvent);

    <T> void publishTopicMsg(MessageQueueEvent<T> messageQueueEvent, String key);


//    /**
//     * 消费消息
//     *
//     * @param exchange
//     * @param queue
//     * @param routingKey
//     * @throws IOException
//     * @throws TimeoutException
//     */
//    void consumerMsg(String exchange,
//                     String queue,
//                     String routingKey) throws IOException, TimeoutException;


//    void processConsumerMsg();
}
