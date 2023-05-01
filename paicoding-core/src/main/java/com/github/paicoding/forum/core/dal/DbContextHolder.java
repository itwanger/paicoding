package com.github.paicoding.forum.core.dal;

/**
 * @author YiHui
 * @date 2023/4/30
 */
public class DbContextHolder {
    /**
     * 使用继承的线程上下文，支持异步时选择传递
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new InheritableThreadLocal<>();


    public static void set(String dbType) {
        CONTEXT_HOLDER.set(dbType);
    }

    public static String get() {
        return CONTEXT_HOLDER.get();
    }

    public static void set(DS ds) {
        set(ds.name().toUpperCase());
    }

    /**
     * 使用主数据源类型
     */
    public static void master() {
        set(DbEnum.MASTER.name());
    }

    /**
     * 使用从数据源类型
     */
    public static void slave() {
        set(DbEnum.SLAVE.name());
    }

    /**
     * 清空数据源定义
     */
    public static void reset() {
        CONTEXT_HOLDER.remove();
    }

}
