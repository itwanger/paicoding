package com.github.paicoding.forum.core.cache;

import com.github.paicoding.forum.api.model.enums.cache.CacheTypeEnum;
import com.github.paicoding.forum.api.model.exception.CacheSyncException;
import com.github.paicoding.forum.core.cache.annotation.CacheKey;
import com.github.paicoding.forum.core.cache.annotation.CacheType;
import com.github.paicoding.forum.core.cache.annotation.CacheValue;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @program: pai_coding
 * @description: 用于读取db到缓存/持久化缓存到db的工具类
 * @author: XuYifei
 * @create: 2024-10-25
 */

@Component
public class CacheSyncUtil {


    /**
     * 读取db中的列表数据，同步到redis中
     * @param key
     * @param clazz 读取的数据类型
     * @param dataList
     * @return
     * @param <T>
     */
    public static <T> boolean syncFromDb2Cache(String key, Class<T> clazz, List<T> dataList) throws IllegalAccessException {

        CacheType annotation = clazz.getAnnotation(CacheType.class);
        if (annotation == null) {
            return false;
        }

        CacheTypeEnum cacheType = annotation.value();

        // Use getDeclaredFields() to access private fields
        Field[] fields = clazz.getDeclaredFields();

        // Iterate over each object in dataList to store each entry in Redis
        for (T data : dataList) {
            Object cacheKey = null, cacheValue = null;

            for (Field field : fields) {
                if (field.isAnnotationPresent(CacheKey.class)) {
                    field.setAccessible(true);
                    // Get the field value from the instance
                    cacheKey = field.get(data);
                    continue;
                }
                if (field.isAnnotationPresent(CacheValue.class)) {
                    field.setAccessible(true);
                    // Get the field value from the instance
                    cacheValue = field.get(data);
                }
            }

            if (cacheType.equals(CacheTypeEnum.CACHE_HASH)) {
                if (cacheKey == null || cacheValue == null) {
                    throw new CacheSyncException(CacheSyncException.CacheSyncExceptionEnum.NO_CACHE_TYPE_ANNOTATION);
                }
                // Store each data entry in Redis with the cacheKey and cacheValue
                RedisClient.hSet(key, cacheKey.toString(), cacheValue.toString());
            }
        }

        // 将数据写入缓存
        return true;
    }
}
