package com.github.paicoding.forum.core.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Louzai
 * @date 2023/5/10
 */
public class RabbitmqUtil {

    /**
     * 每个key都有自己的工厂
     */
    private static Map<String, ConnectionFactory> executors = new ConcurrentHashMap<>();

    /**
     * 初始化一个工厂
     *
     * @param host
     * @param port
     * @param username
     * @param passport
     * @param virtualhost
     * @return
     */
    public static ConnectionFactory init(String host,
                                  Integer port,
                                  String username,
                                  String passport,
                                  String virtualhost) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(passport);
        factory.setVirtualHost(virtualhost);
        return factory;
    }

    /**
     * 工厂单例，每个key都有属于自己的工厂
     *
     * @param key
     * @param host
     * @param port
     * @param username
     * @param passport
     * @param virtualhost
     * @return
     */
    public static ConnectionFactory getOrInitConnectionFactory(String key,
                                                               String host,
                                                               Integer port,
                                                               String username,
                                                               String passport,
                                                               String virtualhost) {
        ConnectionFactory connectionFactory = executors.get(key);
        if (null == connectionFactory) {
            synchronized (RabbitmqUtil.class) {
                connectionFactory = executors.get(key);
                if (null == connectionFactory) {
                    connectionFactory = init(host, port, username, passport, virtualhost);
                    executors.put(key, connectionFactory);
                }
            }
        }
        return connectionFactory;
    }
}
