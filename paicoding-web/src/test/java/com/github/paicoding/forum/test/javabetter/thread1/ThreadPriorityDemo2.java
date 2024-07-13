package com.github.paicoding.forum.test.javabetter.thread1;

public class ThreadPriorityDemo2 {
    // 继承线程类
    static class MyThread extends Thread {
        @Override
        public void run() {
            // 输出当前线程的名字和优先级
            System.out.println("MyThread当前线程：" + Thread.currentThread().getName()
                    + ",优先级：" + Thread.currentThread().getPriority());
        }
    }

    public static void main(String[] args) {
        // 创建 10 个线程，从 1-10 运行，优先级从 1-10
        for (int i = 1; i <= 10; i++) {
            Thread thread = new MyThread();
            thread.setName("线程" + i);
            thread.setPriority(i);
            thread.start();
        }
    }
}
