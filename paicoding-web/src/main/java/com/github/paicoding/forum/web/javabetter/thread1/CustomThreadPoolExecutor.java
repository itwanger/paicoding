package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.*;

/**
 * CustomThreadPoolExecutor is a simple implementation of a thread pool.
 */
public class CustomThreadPoolExecutor {

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit;
    private final BlockingQueue<Runnable> workQueue;
    private final RejectedExecutionHandler handler;

    private volatile boolean isShutdown = false;
    private int currentPoolSize = 0;

    /**
     * Constructs a CustomThreadPoolExecutor.
     *
     * @param corePoolSize    the number of core threads.
     * @param maximumPoolSize the maximum number of threads.
     * @param keepAliveTime   the time to keep extra threads alive.
     * @param unit            the time unit for keepAliveTime.
     * @param workQueue       the queue to hold runnable tasks.
     * @param handler         the handler to use when execution is blocked.
     */
    public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.handler = handler;
    }

    /**
     * Executes a given task using the thread pool.
     *
     * @param task the task to execute.
     */
    public void execute(Runnable task) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool is shutdown");
        }

        synchronized (this) {
            // If current pool size is less than core pool size, create a new worker thread
            if (currentPoolSize < corePoolSize) {
                new Worker(task).start();
                currentPoolSize++;
                return;
            }

            // Try to add task to the queue, if full create a new worker thread if possible
            if (!workQueue.offer(task)) {
                if (currentPoolSize < maximumPoolSize) {
                    new Worker(task).start();
                    currentPoolSize++;
                } else {
                    // If maximum pool size reached, apply the rejection handler
                    handler.rejectedExecution(task, null);
                }
            }
        }
    }

    /**
     * Shuts down the thread pool.
     */
    public void shutdown() {
        isShutdown = true;
    }

    /**
     * Worker is an internal class that represents a worker thread in the pool.
     */
    private class Worker extends Thread {
        private Runnable task;

        Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            while (task != null || (task = getTask()) != null) {
                try {
                    task.run();
                } finally {
                    task = null;
                }
            }
        }

        /**
         * Gets a task from the work queue, waiting up to keepAliveTime if necessary.
         *
         * @return a task to run, or null if the keepAliveTime expires.
         */
        private Runnable getTask() {
            try {
                return workQueue.poll(keepAliveTime, unit);
            } catch (InterruptedException e) {
                return null;
            }
        }
    }
}
