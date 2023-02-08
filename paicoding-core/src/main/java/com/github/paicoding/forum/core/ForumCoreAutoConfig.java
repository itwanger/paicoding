package com.github.paicoding.forum.core;

import com.github.paicoding.forum.core.cache.RedisClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author YiHui
 * @date 2022/9/4
 */
@Configuration
@ComponentScan(basePackages = "com.github.paicoding.forum.core")
public class ForumCoreAutoConfig {

    public ForumCoreAutoConfig(RedisTemplate<String, String> redisTemplate) {
        RedisClient.register(redisTemplate);
    }
}
