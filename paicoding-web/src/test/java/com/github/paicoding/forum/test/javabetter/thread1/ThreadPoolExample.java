package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/26/24
 */
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ThreadPoolExample {
    public static void main(String[] args) {
        // 创建一个核心池大小和最大池大小都为3的固定大小线程池
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 提交4个任务到线程池
        for(int i = 1; i <= 4; i++) {
            int taskID = i;
            executor.execute(() -> {
                System.out.println("任务 " + taskID + " 正在被线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    // 模拟任务执行耗时
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // 关闭线程池
        executor.shutdown();
    }
}
