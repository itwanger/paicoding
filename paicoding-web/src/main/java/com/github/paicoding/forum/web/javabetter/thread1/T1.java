package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.*;

public class T1 {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                      // 核心线程数
                4,                      // 最大线程数
                10,                     // 空闲线程存活时间
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.AbortPolicy()
        );
        Future<Object> future = executor.submit(() -> {
            System.out.println("任务开始");
            int result = 1 / 0; // 除零异常
            return result;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("捕获异常：" + e.getMessage());
        }
    }
}
