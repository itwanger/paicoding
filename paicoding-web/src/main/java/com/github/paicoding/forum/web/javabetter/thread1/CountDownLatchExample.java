package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.CountDownLatch;

class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        int threadCount = 3;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 1000)); // 模拟任务执行
                    System.out.println(Thread.currentThread().getName() + " 执行完毕");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 线程完成后，计数器 -1
                }
            }).start();
        }

        latch.await(); // 主线程等待
        System.out.println("所有子线程执行完毕，主线程继续执行");
    }
}
