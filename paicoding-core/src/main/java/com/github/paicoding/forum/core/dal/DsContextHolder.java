package com.github.paicoding.forum.core.dal;

/**
 * 数据源选择上下持有类，用于存储当前选中的是哪个数据源
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class DsContextHolder {
    /**
     * 使用继承的线程上下文，支持异步时选择传递
     * 使用DsNode，支持链式的数据源切换，如最外层使用master数据源，内部某个方法使用slave数据源；但是请注意，对于事务的场景，不要交叉
     */
    private static final ThreadLocal<DsNode> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    private DsContextHolder() {
    }


    public static void set(String dbType) {
        DsNode current = CONTEXT_HOLDER.get();
        CONTEXT_HOLDER.set(new DsNode(current, dbType));
    }

    public static String get() {
        DsNode ds = CONTEXT_HOLDER.get();
        return ds == null ? null : ds.ds;
    }


    public static void set(DS ds) {
        set(ds.name().toUpperCase());
    }


    /**
     * 移除上下文
     */
    public static void reset() {
        DsNode ds = CONTEXT_HOLDER.get();
        if (ds == null) {
            return;
        }

        if (ds.pre != null) {
            // 退出当前的数据源选择，切回去走上一次的数据源配置
            CONTEXT_HOLDER.set(ds.pre);
        } else {
            CONTEXT_HOLDER.remove();
        }
    }

    /**
     * 使用主数据源类型
     */
    public static void master() {
        set(MasterSlaveDsEnum.MASTER.name());
    }

    /**
     * 使用从数据源类型
     */
    public static void slave() {
        set(MasterSlaveDsEnum.SLAVE.name());
    }

    public static class DsNode {
        DsNode pre;
        String ds;

        public DsNode(DsNode parent, String ds) {
            pre = parent;
            this.ds = ds;
        }
    }

}
