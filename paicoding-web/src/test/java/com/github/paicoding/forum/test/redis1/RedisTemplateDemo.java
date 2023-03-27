package com.github.paicoding.forum.test.redis1;

import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/27/23
 */
@DataRedisTest
public class RedisTemplateDemo {
//    @Autowired
//    private RedisTemplate<Object, Object> redisTemplate;
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Test
//    public void testRedisTemplate() {
//        //设置 key 和 value，并保存到 Redis 中
//        redisTemplate.opsForValue().set("key1", "value1");
//
//        //从 Redis 中获取 key 对应的 value
//        Object value = redisTemplate.opsForValue().get("key1");
//        assertEquals("value1", value.toString());
//    }
//
//    @Test
//    public void testStringRedisTemplate() {
//        //设置 key 和 value，并保存到 Redis 中
//        stringRedisTemplate.opsForValue().set("key2", "value2");
//
//        //从 Redis 中获取 key 对应的 value
//        String value = stringRedisTemplate.opsForValue().get("key2");
//        assertEquals("value2", value);
//    }
}
