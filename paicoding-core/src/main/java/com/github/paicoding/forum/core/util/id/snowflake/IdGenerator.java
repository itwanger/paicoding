package com.github.paicoding.forum.core.util.id.snowflake;

/**
 * @author YiHui
 * @date 2023/10/17
 */
public interface IdGenerator {
    /**
     * 生成分布式id
     *
     * @return
     */
    Long nextId();
}
