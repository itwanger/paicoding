package com.github.paicoding.forum.service.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * redis lua工具类
 *
 * @ClassName: RedisLuaUtil
 * @Author: ygl
 * @Date: 2023/6/2 22:56
 * @Version: 1.0
 */
@Component
@Slf4j
public class RedisLuaUtil {

    private static final String LUA_SCRIPT_CAD = "local current = redis.call('get', KEYS[1]);"
            + "if current == false then "
            + "    return nil;"
            + "end "
            + "if current == ARGV[1] then "
            + "    return redis.call('del', KEYS[1]);"
            + "else "
            + "    return 0;"
            + "end";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 比较并删除
     * @param key
     * @param expect
     * @return 当key不存在时返回null;key存在,当前值=期望值时,删除key;key存在,当前值!=期望值时,返回0;
     */
    public Long cad(String key, String expect) {
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT_CAD, Long.class);
        Long result = null;
        try {
            result = stringRedisTemplate.execute(redisScript, keyList, expect);
        } catch (Exception e) {
            log.error("cad异常", e);
        }
        return result;
    }
}
