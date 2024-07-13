package com.github.paicoding.forum.core.senstive.ibatis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 敏感词缓存
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class SensitiveMetaCache {
    private static ConcurrentHashMap<String, SensitiveObjectMeta> CACHE = new ConcurrentHashMap<>();

    public static SensitiveObjectMeta get(String key) {
        return CACHE.get(key);
    }

    public static void put(String key, SensitiveObjectMeta meta) {
        CACHE.put(key, meta);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static boolean contains(String key) {
        return CACHE.containsKey(key);
    }

    public static SensitiveObjectMeta putIfAbsent(String key, SensitiveObjectMeta meta) {
        return CACHE.putIfAbsent(key, meta);
    }

    public static SensitiveObjectMeta computeIfAbsent(String key, Function<String, SensitiveObjectMeta> function) {
        return CACHE.computeIfAbsent(key, function);
    }
}
