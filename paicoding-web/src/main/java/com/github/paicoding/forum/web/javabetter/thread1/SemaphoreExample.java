package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    private static final int THREAD_COUNT = 5;
    private static final Semaphore semaphore = new Semaphore(2); // 最多允许 2 个线程访问

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire(); // 获取许可（如果没有可用许可，则阻塞）
                    System.out.println(Thread.currentThread().getName() + " 访问资源...");
                    Thread.sleep(2000); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release(); // 释放许可
                }
            }).start();
        }
    }
}