package com.github.paicoding.forum.api.model.enums.cache;

/**
 * @program: pai_coding
 * @description: 标记使用redis的什么数据类型
 * @author: XuYifei
 * @create: 2024-10-25
 */

public enum CacheTypeEnum {

    /**
     * 用于标记缓存类型
     */
    CACHE_STRING("string"),
    CACHE_HASH("hash"),
    CACHE_LIST("list"),
    CACHE_SET("set"),
    CACHE_ZSET("zset")
    ;


    private String CacheType;

    CacheTypeEnum(String cacheType) {
        CacheType = cacheType;
    }
}
