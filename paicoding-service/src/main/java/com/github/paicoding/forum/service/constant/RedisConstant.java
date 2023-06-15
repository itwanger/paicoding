package com.github.paicoding.forum.service.constant;

/**
 * ES 过滤字段常量
 *
 * @ClassName: EsFieldConstant
 * @Author: ygl
 * @Date: 2023/6/2 06:39
 * @Version: 1.0
 */
public class RedisConstant {

    /**
     * article前缀
     */
    public static final String REDIS_PAI = "pai";

    /**
     * article前缀
     */
    public static final String REDIS_PRE_ARTICLE = ":article:";

    /**
     * 缓存
     */
    public static final String REDIS_CACHE = "cache:";

    /**
     * 分布式锁
     */
    public static final String REDIS_LOCK = "lock:";

    /**
     * 点赞/取消点赞
     */
    public static final String PRAISE = "praise:";

    /**
     * 收藏/取消收藏 COLLECTION
     */
    public static final String COLLECTION = "collection:";

}
