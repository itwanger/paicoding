package com.github.paicoding.forum.service.notify.service.impl;

import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.core.rabbitmq.RabbitmqConnection;
import com.github.paicoding.forum.core.rabbitmq.RabbitmqConnectionPool;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.notify.service.RabbitmqService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class RabbitmqServiceImpl implements RabbitmqService {

    // 设置一个消费者的固定连接，从池中获取一个连接即可
    private RabbitmqConnection rabbitmqConsumerConnection;
    private Channel rabbitmqConsumerChannel;

    @Autowired
    private NotifyService notifyService;

    @Override
    public boolean enabled() {
        return "true".equalsIgnoreCase(SpringUtil.getConfig("rabbitmq.switchFlag"));
    }

    @Override
    public void publishMsg(String exchange,
                           BuiltinExchangeType exchangeType,
                           String routingKey,
                           String message) {
        try {
            //创建连接
            RabbitmqConnection rabbitmqConnection = RabbitmqConnectionPool.getConnection();
            Connection connection = rabbitmqConnection.getConnection();
            //创建消息通道
            Channel channel = connection.createChannel();
            // 声明exchange中的消息为可持久化，不自动删除
            channel.exchangeDeclare(exchange, exchangeType, true, false, null);
            // 发布消息
            channel.basicPublish(exchange, routingKey, null, message.getBytes());
            log.info("Publish msg: {}", message);
            channel.close();
            RabbitmqConnectionPool.returnConnection(rabbitmqConnection);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error("rabbitMq消息发送异常: exchange: {}, msg: {}", exchange, message, e);
        }

    }

    /**
     * 阻塞式消费
     * @param exchange
     * @param queueName
     * @param routingKey
     */
    @Override
    public void consumerMsg(String exchange,
                            String queueName,
                            String routingKey) {
        try {
            //创建连接
            rabbitmqConsumerConnection = RabbitmqConnectionPool.getConnection();
            Connection connection = rabbitmqConsumerConnection.getConnection();
            //创建消息信道
            rabbitmqConsumerChannel = connection.createChannel();
            //消息队列
            rabbitmqConsumerChannel.queueDeclare(queueName, true, false, false, null);
            //绑定队列到交换机
            rabbitmqConsumerChannel.queueBind(queueName, exchange, routingKey);

            Consumer consumer = new DefaultConsumer(rabbitmqConsumerChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    log.info("Consumer msg: {}", message);
                    // 获取Rabbitmq消息，并保存到DB
                    // 说明：这里仅作为示例，如果有多种类型的消息，可以根据消息判定，简单的用 if...else 处理，复杂的用工厂 + 策略模式
                    notifyService.saveArticleNotify(JsonUtil.toObj(message, UserFootDO.class), NotifyTypeEnum.PRAISE);
                    rabbitmqConsumerChannel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            // 取消自动ack, 自动监听消息
            rabbitmqConsumerChannel.basicConsume(queueName, false, consumer);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processConsumerMsg() {
        log.info("Begin to processConsumerMsg.");
        consumerMsg(CommonConstants.EXCHANGE_NAME_DIRECT, CommonConstants.QUERE_NAME_PRAISE, CommonConstants.QUERE_KEY_PRAISE);
    }

    /**
     * 关闭连接和通道，销毁时关闭并归还
     */
    @PreDestroy
    public void destroy() {
        try {
            if (rabbitmqConsumerChannel != null && rabbitmqConsumerChannel.isOpen()) {
                rabbitmqConsumerChannel.close();
            }
            if (rabbitmqConsumerConnection != null) {
                RabbitmqConnectionPool.returnConnection(rabbitmqConsumerConnection);
            }
        } catch (IOException | TimeoutException e) {
            log.error("关闭 RabbitMQ 连接和通道时出错", e);
        }
    }
}
