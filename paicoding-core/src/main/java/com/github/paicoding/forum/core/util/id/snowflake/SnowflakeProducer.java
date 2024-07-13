package com.github.paicoding.forum.core.util.id.snowflake;

import com.github.paicoding.forum.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 基于雪花算法计算的id生成器
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class SnowflakeProducer {
    private BlockingQueue<Long> queue;

    /**
     * id失效的间隔时间
     */
    public static final Long ID_EXPIRE_TIME_INTER = DateUtil.ONE_DAY_MILL;
    private static final int QUEUE_SIZE = 10;
    private ExecutorService es = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });

    public SnowflakeProducer(final IdGenerator generator) {
        queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        es.submit(() -> {
            long lastTime = System.currentTimeMillis();
            while (true) {
                try {
                    queue.offer(generator.nextId(), 1, TimeUnit.MINUTES);
                } catch (InterruptedException e1) {
                } catch (Exception e) {
                    log.info("gen id error! {}", e.getMessage());
                }

                // 当出现跨天时，自动重置业务id
                try {
                    long now = System.currentTimeMillis();
                    if (now / ID_EXPIRE_TIME_INTER - lastTime / ID_EXPIRE_TIME_INTER > 0) {
                        // 跨天，清空队列
                        queue.clear();
                        log.info("清空id队列，重新设置");
                    }
                    lastTime = now;

                } catch (Exception e) {
                    log.info("auto remove illegal ids error! {}", e.getMessage());
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
