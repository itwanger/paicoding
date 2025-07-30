package com.github.paicoding.forum.web.javabetter.thread1;

class ReentrantExample {
    public synchronized void method1() {
        System.out.println("Method1 acquired lock");
        method2();  // 线程已经持有锁，能继续调用 method2
    }

    public synchronized void method2() {
        System.out.println("Method2 acquired lock");
    }

    public static void main(String[] args) {
        ReentrantExample example = new ReentrantExample();
        example.method1();
    }
}
