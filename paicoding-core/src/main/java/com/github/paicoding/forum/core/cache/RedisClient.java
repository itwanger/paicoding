package com.github.paicoding.forum.core.cache;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/2/7
 */
public class RedisClient {
    private static final Charset CODE = Charset.forName("UTF-8");
    private static RedisTemplate<String, String> template;

    public static void register(RedisTemplate<String, String> template) {
        RedisClient.template = template;
    }

    public static void nullCheck(Object... args) {
        for (Object obj : args) {
            if (obj == null) {
                throw new IllegalArgumentException("redis argument can not be null!");
            }
        }
    }

    public static byte[] toBytes(String key) {
        nullCheck(key);
        return key.getBytes(CODE);
    }

    public static byte[][] toBytes(List<String> keys) {
        byte[][] bytes = new byte[keys.size()][];
        int index = 0;
        for (String key : keys) {
            bytes[index++] = toBytes(key);
        }
        return bytes;
    }

    public static String getStr(String key) {
        return template.execute((RedisCallback<String>) con -> {
            byte[] val = con.get(toBytes(key));
            return val == null ? null : new String(val);
        });
    }

    public static void setStr(String key, String value) {
        template.execute((RedisCallback<Void>) con -> {
            con.set(toBytes(key), toBytes(value));
            return null;
        });
    }

    public static void del(String key) {
        template.delete(key);
    }

    /**
     * 带过期时间的缓存写入
     *
     * @param key
     * @param value
     * @param expire s为单位
     * @return
     */
    public static Boolean setStrWithExpire(String key, String value, Long expire) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.setEx(toBytes(key), expire, toBytes(value));
            }
        });
    }
}
