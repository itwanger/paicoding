package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.CountDownLatch;

public class SyncExample {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        // 创建3个子线程
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 模拟任务
                    System.out.println("打完王者了.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 每个线程任务完成后计数器减1
                }
            }).start();
        }

        System.out.println("等打完三把王者就去睡觉...");
        latch.await(); // 主线程等待子线程完成
        System.out.println("好，王者玩完了，可以睡了");
    }
}
