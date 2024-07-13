package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/23/23
 */
import java.util.*;
import java.util.concurrent.*;

public class ParallelCalculation {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2;

    private static final ThreadPoolExecutor exec = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            10L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
    );

    public static void main(String[] args) {
        Callable<Double> task = () -> Math.random() * 100;

        List<Future<Double>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(exec.submit(task));
        }

        for (Future<Double> result : results) {
            try {
                System.out.println(result.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        exec.shutdown();
    }
}