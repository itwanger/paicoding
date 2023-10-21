package com.github.paicoding.forum.core.util.id.snowflake;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 基于雪花算法计算的id生成器
 *
 * @author YiHui
 * @date 2023/8/30
 */
@Slf4j
public class SnowflakeProducer {
    private BlockingQueue<Long> queue;
    private static final int QUEUE_SIZE = 2000;
    private ExecutorService es = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });

    public SnowflakeProducer(final IdGenerator generator) {
        queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        es.submit(() -> {
            while (true) {
                try {
                    queue.put(generator.nextId());
                } catch (Exception e) {
                    log.info("gen id error! {}", e.getMessage());
                }
            }
        });
    }

    public Long genId() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("雪花算法生成逻辑异常", e);
            throw new RuntimeException("雪花算法生成id异常!", e);
        }
    }
}
