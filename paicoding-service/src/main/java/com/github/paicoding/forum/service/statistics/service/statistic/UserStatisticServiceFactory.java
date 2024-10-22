package com.github.paicoding.forum.service.statistics.service.statistic;

import com.github.paicoding.forum.service.statistics.service.statistic.impl.UserStatisticServiceAtomicIntegerImpl;
import com.github.paicoding.forum.service.statistics.service.statistic.impl.UserStatisticServiceCaffeineImpl;
import com.github.paicoding.forum.service.statistics.service.statistic.impl.UserStatisticServiceRedisImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @program: pai_coding
 * @description: 动态决定实例化哪一个实现
 * @author: XuYifei
 * @create: 2024-10-21
 */

@Component
@EnableConfigurationProperties(UserStatisticServiceProperties.class)
public class UserStatisticServiceFactory {

    @Bean
    public UserStatisticService userStatisticService(UserStatisticServiceProperties properties) {
        return switch (properties.getType()) {
            case CAFFEINE -> new UserStatisticServiceCaffeineImpl();
            case REDIS -> new UserStatisticServiceRedisImpl();
            case ATOMIC_INTEGER -> new UserStatisticServiceAtomicIntegerImpl();
            default -> throw new IllegalStateException("Unexpected value: " + properties.getType());
        };
    }
}
