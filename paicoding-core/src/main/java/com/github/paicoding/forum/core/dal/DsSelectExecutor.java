package com.github.paicoding.forum.core.dal;

import java.util.function.Supplier;

/**
 * 手动指定数据源的用法
 *
 * @author YiHui
 * @date 2023/4/30
 */
public class DsSelectExecutor {

    public static <T> T execute(DS ds, Supplier<T> supplier) {
        DbContextHolder.set(ds);
        try {
            return supplier.get();
        } finally {
            DbContextHolder.reset();
        }
    }
}
