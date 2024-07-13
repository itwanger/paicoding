package com.github.paicoding.forum.core.util.id.snowflake;

import cn.hutool.core.lang.Snowflake;

import java.util.Date;


/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class HuToolSnowflakeIdGenerator implements IdGenerator {
    private static final Date EPOC = new Date(2023, 1, 1);
    private Snowflake snowflake;

    public HuToolSnowflakeIdGenerator(int workId, int datacenter) {
        snowflake = new Snowflake(EPOC, workId, datacenter, false);
    }

    @Override
    public Long nextId() {
        return snowflake.nextId();
    }
}
