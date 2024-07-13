package com.github.paicoding.forum.core.dal;

/**
 * 主从数据源的枚举
 * @author XuYifei
 * @date 2024-07-12
 */
public enum MasterSlaveDsEnum implements DS {
    /**
     * master主数据源类型
     */
    MASTER,
    /**
     * slave从数据源类型
     */
    SLAVE;
}
