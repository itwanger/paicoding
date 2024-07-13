package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/6/23
 */
import java.util.concurrent.locks.StampedLock;

public class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    // 写方法
    public void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    // 乐观读方法
    public double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x;
        double currentY = y;
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }

    public static void main(String[] args) {
        Point point = new Point();

        // 创建写线程
        Thread writeThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                point.move(i, i);
                System.out.println("写: " + i + ", " + i);
            }
        });

        // 创建读线程
        Thread readThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                double distance = point.distanceFromOrigin();
                System.out.println("距离: " + distance);
            }
        });

        writeThread.start();
        readThread.start();

        try {
            writeThread.join();
            readThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
