package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/23/23
 */
import java.util.concurrent.*;

public class AsynchronousTaskProcessor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 2 * CPU_COUNT;
    private static final int MAX_POOL_SIZE = 2 * CPU_COUNT + 2;

    private static final ThreadPoolExecutor exec = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
    );

    public static void main(String[] args) {
        exec.execute(() -> {
            System.out.println("Async task started");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Async task completed");
        });

        System.out.println("Main thread continues to execute other operations.");
        exec.shutdown();
    }
}