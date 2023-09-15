package com.github.paicoding.forum.core.util.snowflake;

import cn.hutool.core.lang.Snowflake;
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
    private BlockingQueue<Long> queue = new LinkedBlockingQueue<>(2000);
    private ExecutorService es = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });

    private Snowflake snowflake;

    public SnowflakeProducer(int workId, int dataCenter) {
        snowflake = new Snowflake(workId, dataCenter);
        es.submit(() -> {
            while (true) {
                try {

                    if (queue == null) {
                        break;
                    }
                    queue.put(snowflake.nextId());
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
