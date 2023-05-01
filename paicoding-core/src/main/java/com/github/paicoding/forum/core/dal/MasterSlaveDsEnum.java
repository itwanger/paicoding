package com.github.paicoding.forum.core.dal;

/**
 * 主从数据源的枚举
 *
 * @author YiHui
 * @date 2023/4/30
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
