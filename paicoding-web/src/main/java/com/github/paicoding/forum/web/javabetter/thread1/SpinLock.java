package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpinLock {
    private AtomicBoolean lock = new AtomicBoolean(false);

    public void lock() {
        while (!lock.compareAndSet(false, true)) {
            // 自旋等待，不断尝试获取锁
        }
    }

    public void unlock() {
        lock.set(false);
    }

    public static void main(String[] args) {
        SpinLock spinLock = new SpinLock();

        Runnable task = () -> {
            spinLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 获取到锁");
            } finally {
                spinLock.unlock();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
    }
}