package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.*;
import java.util.*;

// 一个简单的线程池实现
class MyThreadPool {
    private int coreSize; // 核心线程数
    private int maxSize;  // 最大线程数
    private BlockingQueue<Runnable> taskQueue; // 任务队列
    private Set<Worker> workers = new HashSet<>(); // 存放工作线程
    private volatile boolean isShutdown = false;   // 线程池是否关闭

    // 构造方法，初始化核心线程数、最大线程数和队列容量
    public MyThreadPool(int coreSize, int maxSize, int queueCapacity) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);

        // 预先启动核心线程
        for (int i = 0; i < coreSize; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            new Thread(worker).start();
        }
    }

    // 提交任务的方法
    public void submit(Runnable task) throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("线程池已经关闭");
        }
        // 如果队列有空间，直接加入任务队列
        if (taskQueue.offer(task)) {
            return;
        }
        // 如果队列满了，但线程数还没到最大，就创建新的线程
        synchronized (this) {
            if (workers.size() < maxSize) {
                Worker worker = new Worker();
                workers.add(worker);
                new Thread(worker).start();
            }
        }
        // 将任务加入队列，注意这里用 put 会阻塞直到有空间
        taskQueue.put(task);
    }

    // 关闭线程池的方法
    public void shutdown() {
        isShutdown = true;
        // 通知所有工作线程停止
        for (Worker worker : workers) {
            worker.stop();
        }
    }

    // 内部类，工作线程
    class Worker implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            // 只要线程池没有关闭或者任务队列不为空，就一直尝试获取任务
            while (running || !taskQueue.isEmpty()) {
                try {
                    // 这里设置了超时获取任务，防止无限阻塞
                    Runnable task = taskQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        task.run();
                    }
                } catch (InterruptedException e) {
                    // 捕获中断异常，继续下一次循环
                }
            }
        }

        // 停止该工作线程
        public void stop() {
            running = false;
        }
    }

    // 测试代码
    public static void main(String[] args) throws InterruptedException {
        MyThreadPool pool = new MyThreadPool(2, 4, 10);
        // 模拟提交 15 个任务
        for (int i = 0; i < 15; i++) {
            int index = i;
            pool.submit(() -> {
                System.out.println("任务 " + index + " 正在被 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(1000); // 模拟任务执行时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        // 等待一段时间后关闭线程池
        Thread.sleep(5000);
        pool.shutdown();
    }
}