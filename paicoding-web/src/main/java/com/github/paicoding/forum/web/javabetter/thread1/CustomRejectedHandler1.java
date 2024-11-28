package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.*;

class CustomRejectedHandler1 {
    public static void main(String[] args) {
        // 自定义拒绝策略
        RejectedExecutionHandler rejectedHandler = (r, executor) -> {
            System.out.println("Task " + r.toString() + " rejected. Queue size: "
                    + executor.getQueue().size());
        };

        // 自定义线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                      // 核心线程数
                4,                      // 最大线程数
                10,                     // 空闲线程存活时间
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),  // 阻塞队列容量
                Executors.defaultThreadFactory(),
                rejectedHandler          // 自定义拒绝策略
        );

        for (int i = 0; i < 10; i++) {
            final int taskNumber = i;
            executor.execute(() -> {
                System.out.println("Executing task " + taskNumber);
                try {
                    Thread.sleep(1000); // 模拟任务耗时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }
}
