package com.github.paicoding.forum.web.javabetter.thread1;

class PrintTask implements Runnable {
    private static int number = 1;        // 共享变量
    private int threadId;                 // 当前线程的编号
    private static final Object lock = new Object();

    public PrintTask(int threadId) {
        this.threadId = threadId;
    }

    @Override
    public void run() {
        while (number <= 100) {
            synchronized (lock) {
                if (number % 3 == threadId) {   // 判断是否当前线程的打印轮次
                    System.out.println("Thread-" + threadId + " prints: " + number++);
                    lock.notifyAll();           // 唤醒其他等待的线程
                } else {
                    try {
                        lock.wait();             // 不是当前线程轮次，进入等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 主线程
    public static void main(String[] args) {
        Thread t1 = new Thread(new PrintTask(0)); // Thread-0
        Thread t2 = new Thread(new PrintTask(1)); // Thread-1
        Thread t3 = new Thread(new PrintTask(2)); // Thread-2

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();  // main 线程等待 t1 完成
            t2.join();  // main 线程等待 t2 完成
            t3.join();  // main 线程等待 t3 完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("所有线程执行完毕，Main 线程开始执行");
    }
}


