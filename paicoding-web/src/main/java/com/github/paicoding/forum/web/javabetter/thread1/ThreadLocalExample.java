package com.github.paicoding.forum.web.javabetter.thread1;

public class ThreadLocalExample {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        threadLocal.set("父线程的值");

        new Thread(() -> {
            System.out.println("子线程获取的值：" + threadLocal.get()); // null
        }).start();
    }
}