package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/31/23
 */
import java.util.concurrent.locks.ReentrantLock;

public class PerformanceTest {
    private static final int NUM_THREADS = 10;
    private static final int NUM_INCREMENTS = 1_000_000;

    private int count1 = 0;
    private int count2 = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Object syncLock = new Object();

    public void increment1() {
        lock.lock();
        try {
            count1++;
        } finally {
            lock.unlock();
        }
    }

    public void increment2() {
        synchronized (syncLock) {
            count2++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PerformanceTest test = new PerformanceTest();

        // Test ReentrantLock
        long startTime = System.nanoTime();
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < NUM_INCREMENTS; j++) {
                    test.increment1();
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long endTime = System.nanoTime();
        System.out.println("ReentrantLock time: " + (endTime - startTime) + " ns");

        // Test synchronized
        startTime = System.nanoTime();
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < NUM_INCREMENTS; j++) {
                    test.increment2();
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        endTime = System.nanoTime();
        System.out.println("synchronized time: " + (endTime - startTime) + " ns");
    }
}

