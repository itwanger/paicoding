package com.github.paicoding.forum.core.rabbitmq;

import com.github.paicoding.forum.core.config.RabbitmqProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class RabbitmqConnectionPool {

    private final BlockingQueue<RabbitmqConnection> pool;

    @Autowired
    private RabbitmqProperties rabbitmqProperties;

    public RabbitmqConnectionPool() {
//        String host = rabbitmqProperties.getHost();
//        Integer port = rabbitmqProperties.getPort();
//        String userName = rabbitmqProperties.getUsername();
//        String password = rabbitmqProperties.getPassport();
//        String virtualhost = rabbitmqProperties.getVirtualhost();
//        Integer poolSize = rabbitmqProperties.getPoolSize();


        String host = "localhost";
        Integer port = 5672;
        String userName = "admin";
        String password = "admin";
        String virtualhost = "/";
        Integer poolSize = 10;
                pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.add(new RabbitmqConnection(host, port, userName, password, virtualhost));
        }
    }

    public RabbitmqConnection getConnection() throws InterruptedException {
        return pool.take();
    }

    public void returnConnection(RabbitmqConnection connection) {
        pool.add(connection);
    }

    public void close() {
        pool.forEach(RabbitmqConnection::close);
    }
}
