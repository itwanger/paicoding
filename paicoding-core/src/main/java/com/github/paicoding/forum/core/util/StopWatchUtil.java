package com.github.paicoding.forum.core.util;

import org.springframework.util.StopWatch;

import java.util.concurrent.Callable;

/**
 * 统计耗时工具类
 *
 * @author YiHui
 * @date 2023/11/10
 */
public class StopWatchUtil {
    private StopWatch stopWatch;

    private StopWatchUtil(String task) {
        stopWatch = task == null ? new StopWatch() : new StopWatch(task);
    }

    /**
     * 初始化
     *
     * @param task
     * @return
     */
    public static StopWatchUtil init(String... task) {
        return new StopWatchUtil(task.length > 0 ? task[0] : null);
    }

    /**
     * 计时
     *
     * @param task 任务名
     * @param call 执行业务逻辑
     * @param <T>  返回类型
     * @return 返回结果
     */
    public <T> T record(String task, Callable<T> call) {
        stopWatch.start(task);
        try {
            return call.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
        }
    }

    /**
     * 计时
     *
     * @param task 任务名
     * @param run  执行业务逻辑
     */
    public void record(String task, Runnable run) {
        stopWatch.start(task);
        try {
            run.run();
        } finally {
            stopWatch.stop();
        }
    }


    /**
     * 计时信息输出
     *
     * @return
     */
    public String prettyPrint() {
        return stopWatch.prettyPrint();
    }
}
