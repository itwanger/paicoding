package com.github.paicoding.forum.service.statistics.service.statistic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: pai_coding
 * @description: 控制实例化的在线人数统计信息配置
 * @author: XuYifei
 * @create: 2024-10-21
 */

@Data
@Valid
@ConfigurationProperties(prefix = "online.statistics")
public class UserStatisticServiceProperties {

    @Getter
    public enum UserStatisticServiceType{

        /**
         * 使用caffeine缓存实现在线人数的统计
         */
        CAFFEINE("caffeine"),
        /**
         * 使用redis实现在线人数的统计
         */
        REDIS("redis"),
        /**
         * 使用内存的原子整型AtomicInteger
         */
        ATOMIC_INTEGER("atomicInteger");

        private final String serviceType;

        UserStatisticServiceType(String caffeine) {
            this.serviceType = caffeine;
        }
    }

    @NotNull(message = "online.statistics.type 不能为空")
    private UserStatisticServiceType type;

}
