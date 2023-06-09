package com.github.paicoding.forum.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ配置文件
 *
 * @author LouZai
 * @since 2023/5/10
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitmqProperties {

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String passport;

    /**
     * 路径
     */
    private String virtualhost;

    /**
     * 连接池大小
     */
    private Integer poolSize;

    /**
     * 开关 false-关闭，true-打开
     */
    private Boolean switchFlag;
}
