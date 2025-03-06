package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CountDownLatchQueryExample {

    private static final int THREAD_COUNT = 20;  // 线程数
    private static final int TOTAL_RECORDS = 100000;  // 数据总量
    private static final int BATCH_SIZE = TOTAL_RECORDS / THREAD_COUNT; // 每个线程处理的数量

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        Semaphore semaphore = new Semaphore(THREAD_COUNT); // 信号量，初始值 20
        List<String> results = new CopyOnWriteArrayList<>(); // 线程安全列表，存放查询结果

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int start = i * BATCH_SIZE;
            final int end = (i == THREAD_COUNT - 1) ? TOTAL_RECORDS : start + BATCH_SIZE;

            threadPool.execute(() -> {
                try {
                    List<String> partialResults = queryData(start, end); // 模拟查询数据
                    results.addAll(partialResults);  // 存入结果集
                } finally {
                    semaphore.release(); // 任务完成，释放信号量
                }
            });
        }

        semaphore.acquire(THREAD_COUNT); // 主线程阻塞，直到所有信号量被释放
        System.out.println("所有查询任务已完成，最终结果：" + results.size());

        threadPool.shutdown(); // 关闭线程池
    }

    // 模拟查询数据
    private static List<String> queryData(int start, int end) {
        List<String> data = new ArrayList<>();
        for (int i = start; i < end; i++) {
            data.add("记录-" + i);
        }
        return data;
    }
}