package com.github.paicoding.forum.core.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class RabbitmqConnection {

    private Connection connection;

    public RabbitmqConnection(String host, int port, String userName, String password, String virtualhost) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualhost);
        try {
            connection = connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取链接
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 关闭链接
     *
     */
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
