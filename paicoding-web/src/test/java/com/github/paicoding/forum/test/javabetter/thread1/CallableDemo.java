package com.github.paicoding.forum.test.javabetter.thread1;

import java.util.concurrent.*;

public class CallableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建一个包含5个线程的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // 创建一个Callable任务
        Callable<String> task = new Callable<String>() {
            public String call() {
                return "Hello from " + Thread.currentThread().getName();
            }
        };

        // 提交任务到ExecutorService执行，并获取Future对象
        Future[] futures = new Future[10];
        for (int i = 0; i < 10; i++) {
            futures[i] = executorService.submit(task);
        }

        // 通过Future对象获取任务的结果
        for (int i = 0; i < 10; i++) {
            System.out.println(futures[i].get());
        }

        // 关闭ExecutorService，不再接受新的任务，等待所有已提交的任务完成
        executorService.shutdown();
    }

    static void runnableDemo() {
        // 创建一个包含5个线程的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // 创建一个Runnable任务
        Runnable task = new Runnable() {
            public void run() {
                System.out.println("Hello from " + Thread.currentThread().getName());
            }
        };

        // 提交任务到ExecutorService执行
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        // 关闭ExecutorService，不再接受新的任务，等待所有已提交的任务完成
        executorService.shutdown();
    }
}
