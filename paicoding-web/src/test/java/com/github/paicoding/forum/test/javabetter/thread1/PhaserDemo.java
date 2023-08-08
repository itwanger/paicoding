package com.github.paicoding.forum.test.javabetter.thread1;

import java.util.concurrent.Phaser;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/6/23
 */
public class PhaserDemo {

    public static void main(String[] args) {
        Phaser phaser = new Phaser(3); // 3 个线程共同完成任务

        new Thread(new Task(phaser), "Thread 1").start();
        new Thread(new Task(phaser), "Thread 2").start();
        new Thread(new Task(phaser), "Thread 3").start();
    }

    static class Task implements Runnable {
        private final Phaser phaser;

        public Task(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " 完成了第一步操作");
            phaser.arriveAndAwaitAdvance(); // 等待其他线程完成第一步操作
            System.out.println(Thread.currentThread().getName() + " 完成了第二步操作");
            phaser.arriveAndAwaitAdvance(); // 等待其他线程完成第二步操作
            System.out.println(Thread.currentThread().getName() + " 完成了第三步操作");
            phaser.arriveAndAwaitAdvance(); // 等待其他线程完成第三步操作
        }
    }
}