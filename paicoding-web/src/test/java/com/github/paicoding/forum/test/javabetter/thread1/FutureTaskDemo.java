package com.github.paicoding.forum.test.javabetter.thread1;

import java.util.concurrent.*;

public class FutureTaskDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建一个固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // 创建一系列 Callable
        Callable<Integer>[] tasks = new Callable[5];
        for (int i = 0; i < tasks.length; i++) {
            final int index = i;
            tasks[i] = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    TimeUnit.SECONDS.sleep(index + 1);
                    return (index + 1) * 100;
                }
            };
        }

        // 将 Callable 包装为 FutureTask，并提交到线程池
        FutureTask<Integer>[] futureTasks = new FutureTask[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            futureTasks[i] = new FutureTask<>(tasks[i]);
            executorService.submit(futureTasks[i]);
        }

        // 获取任务结果
        for (int i = 0; i < futureTasks.length; i++) {
            System.out.println("Result of task" + (i + 1) + ": " + futureTasks[i].get());
        }

        // 关闭线程池
        executorService.shutdown();
    }
}
