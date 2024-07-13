package com.github.paicoding.forum.test.javabetter.thread1;

public class ThreadGroupDemo2 {
    public static void main(String[] args) {
        // 创建一个线程组，并重新定义异常
        ThreadGroup group = new ThreadGroup("testGroup") {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(t.getName() + ": " + e.getMessage());
            }
        };

        // 测试异常
        Thread thread = new Thread(group, () -> {
            // 抛出 unchecked 异常
            throw new RuntimeException("测试异常");
        });

        // 启动线程
        thread.start();

    }
}
