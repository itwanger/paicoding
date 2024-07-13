package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/6/23
 */
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SharedResourceWithReentrantReadWriteLock {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private int data = 0;

    public void write(int value) {
        lock.writeLock().lock();
        try {
            data = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int read() {
        lock.readLock().lock();
        try {
            return data;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void main(String[] args) {
        SharedResourceWithReentrantReadWriteLock sharedResource = new SharedResourceWithReentrantReadWriteLock();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                sharedResource.write(i);
                System.out.println("Write: " + i);
            }
        });

        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                int value = sharedResource.read();
                System.out.println("Read: " + value);
            }
        });

        writer.start();
        reader.start();
    }
}

