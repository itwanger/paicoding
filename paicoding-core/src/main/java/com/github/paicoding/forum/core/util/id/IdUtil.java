package com.github.paicoding.forum.core.util.id;

import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.util.CompressUtil;
import com.github.paicoding.forum.core.util.id.snowflake.PaiSnowflakeIdGenerator;
import com.github.paicoding.forum.core.util.id.snowflake.SnowflakeProducer;

import static com.github.paicoding.forum.core.util.CompressUtil.int2str;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class IdUtil {
    /**
     * 默认的id生成器
     */
    public static SnowflakeProducer DEFAULT_ID_PRODUCER = new SnowflakeProducer(new PaiSnowflakeIdGenerator());

    /**
     * 生成全局id
     *
     * @return
     */
    public static Long genId() {
        return DEFAULT_ID_PRODUCER.genId();
    }

    /**
     * 生成字符串格式全局id
     *
     * @return
     */
    public static String genStrId() {
        return CompressUtil.int2str(genId());
    }

    public static void main(String[] args) {
        System.out.println(IdUtil.genStrId());
        Long id = IdUtil.genId();
        System.out.println(id + " = " + int2str(id));
        System.out.println(IdUtil.genId() + "->" + IdUtil.genStrId());
        AsyncUtil.sleep(2000);
        System.out.println(IdUtil.genId() + "->" + IdUtil.genStrId());

        System.out.println("-----");

        SnowflakeProducer producer = new SnowflakeProducer(new PaiSnowflakeIdGenerator());
        id = producer.genId();
        System.out.println("id: " + id + " -> " + int2str(id));
        AsyncUtil.sleep(3000L);
        id = producer.genId();
        System.out.println("id: " + id + " -> " + int2str(id));
    }
}
