package com.github.paicoding.forum.core.cache;

import com.github.paicoding.forum.core.util.JsonUtil;
import com.google.common.collect.Maps;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2023/2/7
 */
public class RedisClient {
    private static final Charset CODE = StandardCharsets.UTF_8;
    private static final String KEY_PREFIX = "pai_";
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

    /**
     * 技术派的缓存值序列化处理
     *
     * @param val
     * @param <T>
     * @return
     */
    public static <T> byte[] valBytes(T val) {

        if (val instanceof String) {
            return ((String) val).getBytes(CODE);
        } else {
            return JsonUtil.toStr(val).getBytes(CODE);
        }
    }

    /**
     * 生成技术派的缓存key
     *
     * @param key
     * @return
     */
    public static byte[] keyBytes(String key) {
        nullCheck(key);
        key = KEY_PREFIX + key;
        return key.getBytes(CODE);
    }

    public static byte[][] keyBytes(List<String> keys) {
        byte[][] bytes = new byte[keys.size()][];
        int index = 0;
        for (String key : keys) {
            bytes[index++] = keyBytes(key);
        }
        return bytes;
    }

    /**
     * 查询缓存
     *
     * @param key
     * @return
     */
    public static String getStr(String key) {
        return template.execute((RedisCallback<String>) con -> {
            byte[] val = con.get(keyBytes(key));
            return val == null ? null : new String(val);
        });
    }

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     */
    public static void setStr(String key, String value) {
        template.execute((RedisCallback<Void>) con -> {
            con.set(keyBytes(key), valBytes(value));
            return null;
        });
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public static void del(String key) {
        template.execute((RedisCallback<Long>) con -> con.del(keyBytes(key)));
    }

    /**
     * 设置缓存有效期
     *
     * @param key
     * @param expire
     */
    public static void expire(String key, Long expire) {
        template.execute((RedisCallback<Void>) connection -> {
            connection.expire(keyBytes(key), expire);
            return null;
        });
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
                return redisConnection.setEx(keyBytes(key), expire, valBytes(value));
            }
        });
    }

    public static <T> Map<String, T> hGetAll(String key, Class<T> clz) {
        Map<byte[], byte[]> records = template.execute((RedisCallback<Map<byte[], byte[]>>) con -> con.hGetAll(keyBytes(key)));
        if (records == null) {
            return Collections.emptyMap();
        }

        Map<String, T> result = Maps.newHashMapWithExpectedSize(records.size());
        for (Map.Entry<byte[], byte[]> entry : records.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            result.put(new String(entry.getKey()), toObj(entry.getValue(), clz));
        }
        return result;
    }

    public static <T> T hGet(String key, String field, Class<T> clz) {
        return template.execute((RedisCallback<T>) con -> {
            byte[] records = con.hGet(keyBytes(key), valBytes(field));
            if (records == null) {
                return null;
            }

            return toObj(records, clz);
        });
    }

    /**
     * 自增
     *
     * @param key
     * @param filed
     * @param cnt
     * @return
     */
    public static Long hIncr(String key, String filed, Integer cnt) {
        return template.execute((RedisCallback<Long>) con -> con.hIncrBy(keyBytes(key), valBytes(filed), cnt));
    }

    public static <T> Boolean hDel(String key, String field) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hDel(keyBytes(key), valBytes(field)) > 0;
            }
        });
    }

    public static <T> Boolean hSet(String key, String field, T ans) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.hSet(keyBytes(key), valBytes(field), valBytes(ans));
            }
        });
    }

    public static <T> void hMSet(String key, Map<String, T> fields) {
        Map<byte[], byte[]> val = Maps.newHashMapWithExpectedSize(fields.size());
        for (Map.Entry<String, T> entry : fields.entrySet()) {
            val.put(valBytes(entry.getKey()), valBytes(entry.getValue()));
        }
        template.execute((RedisCallback<Object>) connection -> {
            connection.hMSet(keyBytes(key), val);
            return null;
        });
    }

    /**
     * 判断value是否再set中
     *
     * @param key
     * @param value
     * @return
     */
    public static <T> Boolean sIsMember(String key, T value) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.sIsMember(keyBytes(key), valBytes(value));
            }
        });
    }

    /**
     * 获取set中的所有内容
     *
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> Set<T> sGetAll(String key, Class<T> clz) {
        return template.execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> set = connection.sMembers(keyBytes(key));
                if (CollectionUtils.isEmpty(set)) {
                    return Collections.emptySet();
                }
                return set.stream().map(s -> toObj(s, clz)).collect(Collectors.toSet());
            }
        });
    }

    /**
     * 往set中添加内容
     *
     * @param key
     * @param val
     * @param <T>
     * @return
     */
    public static <T> boolean sPut(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.sAdd(keyBytes(key), valBytes(val));
            }
        }) > 0;
    }

    /**
     * 移除set中的内容
     *
     * @param key
     * @param val
     * @param <T>
     */
    public static <T> void sDel(String key, T val) {
        template.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                connection.sRem(keyBytes(key), valBytes(val));
                return null;
            }
        });
    }


    public static <T> Long lPush(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lPush(keyBytes(key), valBytes(val));
            }
        });
    }

    public static <T> Long rPush(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.rPush(keyBytes(key), valBytes(val));
            }
        });
    }

    public static <T> List<T> lRange(String key, int start, int size, Class<T> clz) {
        return template.execute(new RedisCallback<List<T>>() {

            @Override
            public List<T> doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> list = connection.lRange(keyBytes(key), start, size);
                if (CollectionUtils.isEmpty(list)) {
                    return new ArrayList<>();
                }
                return list.stream().map(k -> toObj(k, clz)).collect(Collectors.toList());
            }
        });
    }

    public static void lTrim(String key, int start, int size) {
        template.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                connection.lTrim(keyBytes(key), start, size);
                return null;
            }
        });
    }

    private static <T> T toObj(byte[] ans, Class<T> clz) {
        if (clz == String.class) {
            return (T) new String(ans, CODE);
        }

        return JsonUtil.toObj(new String(ans, CODE), clz);
    }
}
