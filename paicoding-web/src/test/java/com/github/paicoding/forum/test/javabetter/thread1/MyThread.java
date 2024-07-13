package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/25/24
 */
class MyThread extends Thread {
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        t1.start(); // 正确的方式，创建一个新线程，并在新线程中执行 run()
        t1.run(); // 仅在主线程中执行 run()，没有创建新线程
    }
}