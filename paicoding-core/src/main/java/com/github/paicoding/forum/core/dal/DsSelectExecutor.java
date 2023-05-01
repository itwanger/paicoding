package com.github.paicoding.forum.core.dal;

import java.util.function.Supplier;

/**
 * 手动指定数据源的用法
 *
 * @author YiHui
 * @date 2023/4/30
 */
public class DsSelectExecutor {

    /**
     * 有返回结果
     *
     * @param ds
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> T submit(DS ds, Supplier<T> supplier) {
        DbContextHolder.set(ds);
        try {
            return supplier.get();
        } finally {
            DbContextHolder.reset();
        }
    }

    /**
     * 无返回结果
     *
     * @param ds
     * @param call
     */
    public static void execute(DS ds, Runnable call) {
        DbContextHolder.set(ds);
        try {
            call.run();
        } finally {
            DbContextHolder.reset();
        }
    }
}
