package com.github.paicoding.forum.web.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @program: pai_coding
 * @description: 本地缓存配置
 * @author: XuYifei
 * @create: 2024-10-21
 */

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Boolean> sessionCache(){
        // 创建缓存
        return Caffeine.newBuilder()
                // 设置缓存过期时间
                .expireAfterAccess(30, TimeUnit.SECONDS)
                // 设置缓存最大容量
                .maximumSize(10_1000)
                .build();
    }

}
