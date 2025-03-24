package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ForkJoinExample {
    public static void main(String[] args) {
        int[] arr = new int[100];
        for (int i = 0; i < 100; i++) {
            arr[i] = i + 1; // 填充数据 1 到 100
        }

        // 创建 ForkJoinPool，默认使用可用的处理器核心数
        ForkJoinPool pool = new ForkJoinPool();

        // 创建 ForkJoin 任务
        SumTask task = new SumTask(arr, 0, arr.length);

        // 执行任务
        Integer result = pool.invoke(task);

        System.out.println("数组的和是: " + result);
    }

    // 自定义任务，继承 RecursiveTask
    static class SumTask extends RecursiveTask<Integer> {
        private int[] arr;
        private int start;
        private int end;

        public SumTask(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= 10) {  // 如果任务足够小，就直接计算
                int sum = 0;
                for (int i = start; i < end; i++) {
                    sum += arr[i];
                }
                return sum;
            } else {
                // 否则拆分任务
                int mid = (start + end) / 2;
                SumTask left = new SumTask(arr, start, mid);
                SumTask right = new SumTask(arr, mid, end);

                // 分别执行子任务
                left.fork();
                right.fork();

                // 合并结果
                int leftResult = left.join();
                int rightResult = right.join();

                return leftResult + rightResult;  // 汇总结果
            }
        }
    }
}