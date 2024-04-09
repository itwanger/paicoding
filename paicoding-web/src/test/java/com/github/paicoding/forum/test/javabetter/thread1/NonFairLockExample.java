package com.github.paicoding.forum.test.javabetter.thread1;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/23/24
 */
public class NonFairLockExample {
    private final ReentrantLock lock = new ReentrantLock(); // 默认为非公平锁

    public void print(String message) {
        lock.lock();  // 请求锁
        try {
            System.out.println(message);
        } finally {
            lock.unlock();  // 释放锁
        }
    }

    public static void main(String[] args) {
        NonFairLockExample example = new NonFairLockExample();

        Thread t1 = new Thread(() -> example.print("Thread 1"));
        Thread t2 = new Thread(() -> example.print("Thread 2"));
        Thread t3 = new Thread(() -> example.print("Thread 3"));

        t1.start();
        t2.start();
        t3.start();
    }
}
