package com.github.paicoding.forum.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMQ配置文件
 *
 * @author XuYifei
 * @since 2024-07-12
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
