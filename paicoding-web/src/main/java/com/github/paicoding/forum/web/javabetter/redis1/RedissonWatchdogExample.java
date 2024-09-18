package com.github.paicoding.forum.web.javabetter.redis1;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissonWatchdogExample {
    public static void main(String[] args) {
        // 配置 Redisson 客户端
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redisson = Redisson.create(config);

        // 获取锁对象
        RLock lock = redisson.getLock("myLock");

        try {
            // 获取锁，默认看门狗机制会启动
            lock.lock();

            // 模拟任务执行
            System.out.println("Task is running...");
            Thread.sleep(40000); // 模拟长时间任务（40秒）

            System.out.println("Task completed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            lock.unlock();
        }

        // 关闭 Redisson 客户端
        redisson.shutdown();
    }
}