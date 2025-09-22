package com.github.paicoding.forum.web.javabetter.shejimoshi;

import java.util.concurrent.ThreadFactory;

class NamedThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count = 0;

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(prefix + "-" + count++);
        return thread;
    }
}

public class ThreadFactoryDemo {
    public static void main(String[] args) {
        ThreadFactory factory = new NamedThreadFactory("MyThread");

        Runnable task = () -> {
            System.out.println("线程名称: " + Thread.currentThread().getName());
        };

        for (int i = 0; i < 5; i++) {
            Thread thread = factory.newThread(task);
            thread.start();
        }
    }
}