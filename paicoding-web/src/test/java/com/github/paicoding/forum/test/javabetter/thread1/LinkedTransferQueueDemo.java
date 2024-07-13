package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/18/23
 */
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class LinkedTransferQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        LinkedTransferQueue<String> queue = new LinkedTransferQueue<>();

        // Consumer Thread
        new Thread(() -> {
            try {
                System.out.println("消费者正在等待获取元素...");
                String element = queue.take();
                System.out.println("消费者收到: " + element);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Let consumer thread start first
        TimeUnit.SECONDS.sleep(1);

        // Producer Thread
        System.out.println("生产者正在传输元素");
        queue.transfer("Hello, World!");

        System.out.println("生产者已转移元素");
    }
}
