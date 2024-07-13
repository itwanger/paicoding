package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/6/23
 */
import java.util.concurrent.locks.StampedLock;

public class SharedResourceWithStampedLock {
    private final StampedLock sl = new StampedLock();
    private int data = 0;

    public void write(int value) {
        long stamp = sl.writeLock();
        try {
            data = value;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public int read() {
        long stamp = sl.tryOptimisticRead();
        int currentData = data;
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                currentData = data;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return currentData;
    }

    public static void main(String[] args) {
        SharedResourceWithStampedLock sharedResource = new SharedResourceWithStampedLock();

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
