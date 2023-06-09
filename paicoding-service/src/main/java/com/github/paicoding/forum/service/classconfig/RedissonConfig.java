package com.github.paicoding.forum.service.classconfig;


import java.io.IOException;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * redisson 配置类
 *
 * @ClassName: RedisLuaUtil
 * @Author: ygl
 * @Date: 2023/6/2 22:56
 * @Version: 1.0
 */
@Configuration
@Slf4j
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    /**
     * RedissonClient,单机模式
     *
     */
    @Bean
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        RedissonClient redissonClient = null;
        try {
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            redissonClient = Redisson.create(config);
        } catch (Exception e) {

            log.info("Redis 连接异常");

        }

        return redissonClient;
    }
}