package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * CustomRejectedExecutionHandler contains several common rejection policies.
 */
public class CustomRejectedExecutionHandler {

    /**
     * AbortPolicy throws a RuntimeException when the task is rejected.
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RuntimeException("Task " + r.toString() + " rejected from " + e.toString());
        }
    }

    /**
     * DiscardPolicy silently discards the rejected task.
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            // Do nothing
        }
    }

    /**
     * CallerRunsPolicy runs the rejected task in the caller's thread.
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }
}