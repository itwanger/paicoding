package com.github.paicoding.forum.core.async;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.github.paicoding.forum.core.util.EnvUtil;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 异步工具类
 *
 * @author XuYifei
 * @date 2024/7/8
 */
@Slf4j
public class AsyncUtil {
    private static final TransmittableThreadLocal<CompletableFutureBridge> THREAD_LOCAL = new TransmittableThreadLocal<>();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = this.defaultFactory.newThread(r);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }

            thread.setName("paicoding-" + this.threadNumber.getAndIncrement());
            return thread;
        }
    };
    private static ExecutorService executorService;
    private static SimpleTimeLimiter simpleTimeLimiter;

    static {
        initExecutorService(Runtime.getRuntime().availableProcessors() * 2, 50);
    }

    public static void initExecutorService(int core, int max) {
        // 异步工具类的默认线程池构建, 参数选择原则:
        //  1. 技术派不存在cpu密集型任务，大部分操作都设计到 redis/mysql 等io操作
        //  2. 统一的异步封装工具，这里的线程池是一个公共的执行仓库，不希望被其他的线程执行影响，因此队列长度为0, 核心线程数满就创建线程执行，超过最大线程，就直接当前线程执行
        //  3. 同样因为属于通用工具类，再加上技术派的异步使用的情况实际上并不是非常饱和的，因此空闲线程直接回收掉即可；大部分场景下，cpu * 2的线程数即可满足要求了
        max = Math.max(core, max);
        executorService = new ExecutorBuilder()
                .setCorePoolSize(core)
                .setMaxPoolSize(max)
                .setKeepAliveTime(0)
                .setKeepAliveTime(0, TimeUnit.SECONDS)
                .setWorkQueue(new SynchronousQueue<Runnable>())
                .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .setThreadFactory(THREAD_FACTORY)
                .buildFinalizable();
        // 包装一下线程池，避免出现上下文复用场景
        executorService = TtlExecutors.getTtlExecutorService(executorService);
        simpleTimeLimiter = SimpleTimeLimiter.create(executorService);
    }


    /**
     * 带超时时间的方法调用执行，当执行时间超过给定的时间，则返回一个超时异常，内部的任务还是正常执行
     * 若超时时间内执行完毕，则直接返回
     *
     * @param time
     * @param unit
     * @param call
     * @param <T>
     * @return
     */
    public static <T> T callWithTimeLimit(long time, TimeUnit unit, Callable<T> call) throws ExecutionException, InterruptedException, TimeoutException {
        return simpleTimeLimiter.callWithTimeout(call, time, unit);
    }


    public static void execute(Runnable call) {
        executorService.execute(call);
    }

    public static <T> Future<T> submit(Callable<T> t) {
        return executorService.submit(t);
    }


    public static boolean sleep(Number timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
            return true;
        } catch (InterruptedException var3) {
            return false;
        }
    }

    public static boolean sleep(Number millis) {
        return millis == null ? true : sleep(millis.longValue());
    }

    public static boolean sleep(long millis) {
        if (millis > 0L) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException var3) {
                return false;
            }
        }

        return true;
    }


    public static class CompletableFutureBridge implements Closeable {
        private List<CompletableFuture> list;
        private Map<String, Long> cost;
        private String taskName;
        private boolean markOver;
        private ExecutorService executorService;

        public CompletableFutureBridge() {
            this(AsyncUtil.executorService, "CompletableFutureExecute");
        }

        public CompletableFutureBridge(ExecutorService executorService, String task) {
            this.taskName = task;
            list = new CopyOnWriteArrayList<>();
            // 支持排序的耗时记录
            cost = new ConcurrentSkipListMap<>();
            cost.put(task, System.currentTimeMillis());
            this.executorService = TtlExecutors.getTtlExecutorService(executorService);
            this.markOver = true;
        }

        /**
         * 异步执行，带返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @return
         */
        public <T> CompletableFutureBridge async(Supplier<T> supplier, String name) {
            list.add(CompletableFuture.supplyAsync(supplyWithTime(supplier, name), this.executorService));
            return this;
        }

        /**
         * 同步执行，待返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @param <T>      返回类型
         * @return 任务的执行返回结果
         */
        public <T> T sync(Supplier<T> supplier, String name) {
            return supplyWithTime(supplier, name).get();
        }

        /**
         * 异步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public CompletableFutureBridge async(Runnable run, String name) {
            list.add(CompletableFuture.runAsync(runWithTime(run, name), this.executorService));
            return this;
        }

        /**
         * 同步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public CompletableFutureBridge sync(Runnable run, String name) {
            runWithTime(run, name).run();
            return this;
        }

        private Runnable runWithTime(Runnable run, String name) {
            return () -> {
                startRecord(name);
                try {
                    run.run();
                } finally {
                    endRecord(name);
                }
            };
        }

        private <T> Supplier<T> supplyWithTime(Supplier<T> call, String name) {
            return () -> {
                startRecord(name);
                try {
                    return call.get();
                } finally {
                    endRecord(name);
                }
            };
        }

        public CompletableFutureBridge allExecuted() {
            if (!CollectionUtils.isEmpty(list)) {
                CompletableFuture.allOf(ArrayUtil.toArray(list, CompletableFuture.class)).join();
            }
            this.markOver = true;
            endRecord(this.taskName);
            return this;
        }

        private void startRecord(String name) {
            cost.put(name, System.currentTimeMillis());
        }

        private void endRecord(String name) {
            long now = System.currentTimeMillis();
            long last = cost.getOrDefault(name, now);
            if (last >= now / 1000) {
                // 之前存储的是时间戳，因此我们需要更新成执行耗时 ms单位
                cost.put(name, now - last);
            }
        }

        public void prettyPrint() {
            if (EnvUtil.isPro()) {
                // 生产环境默认不打印执行耗时日志
                return;
            }

            if (!this.markOver) {
                // 在格式化输出时，要求所有任务执行完毕
                this.allExecuted();
            }

            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            long totalCost = cost.remove(taskName);
            sb.append("StopWatch '").append(taskName).append("': running time = ").append(totalCost).append(" ms");
            sb.append('\n');
            if (cost.size() <= 1) {
                sb.append("No task info kept");
            } else {
                sb.append("---------------------------------------------\n");
                sb.append("ms         %     Task name\n");
                sb.append("---------------------------------------------\n");
                NumberFormat pf = NumberFormat.getPercentInstance();
                pf.setMinimumIntegerDigits(2);
                pf.setMinimumFractionDigits(2);
                pf.setGroupingUsed(false);
                for (Map.Entry<String, Long> entry : cost.entrySet()) {
                    sb.append(entry.getValue()).append("\t\t");
                    sb.append(pf.format(entry.getValue() / (double) totalCost)).append("\t\t");
                    sb.append(entry.getKey()).append("\n");
                }
            }

            log.info("\n---------------------\n{}\n--------------------\n", sb);
        }

        @Override
        public void close() {
            try {
                if (!this.markOver) {
                    // 做一个兜底，避免业务侧没有手动结束，导致异步任务没有执行完就提前返回结果
                    this.allExecuted();
                }

                AsyncUtil.release();
                prettyPrint();
            } catch (Exception e) {
                log.error("释放耗时上下文异常! {}", taskName, e);
            }
        }
    }

    public static CompletableFutureBridge concurrentExecutor(String... name) {
        if (name.length > 0) {
            return new CompletableFutureBridge(AsyncUtil.executorService, name[0]);
        }
        return new CompletableFutureBridge();
    }

    /**
     * 开始桥接类
     *
     * @param executorService 线程池
     * @param name            标记名
     * @return 桥接类
     */
    public static CompletableFutureBridge startBridge(ExecutorService executorService, String name) {
        CompletableFutureBridge bridge = new CompletableFutureBridge(executorService, name);
        THREAD_LOCAL.set(bridge);
        return bridge;
    }

    /**
     * 获取计时桥接类
     *
     * @return 桥接类
     */
    public static CompletableFutureBridge getBridge() {
        return THREAD_LOCAL.get();
    }

    /**
     * 释放统计
     */
    public static void release() {
        THREAD_LOCAL.remove();
    }
}
