package com.github.paicoding.forum.test.javabetter.redis1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/25/24
 */
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonManager {
    private static RedissonClient client;

    public static void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        client = Redisson.create(config);
    }

    public static RedissonClient getClient() {
        return client;
    }
}
