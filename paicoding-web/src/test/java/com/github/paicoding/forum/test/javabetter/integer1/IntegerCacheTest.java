package com.github.paicoding.forum.test.javabetter.integer1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/23/24
 */
class IntegerCacheTest {
    public static void main(String[] args) {
        Integer a = 127; // 在默认缓存范围内
        Integer b = 127; // 在默认缓存范围内

        Integer c = 1000; // 超出默认缓存范围，但在我们设置的范围内
        Integer d = 1000; // 超出默认缓存范围，但在我们设置的范围内

        System.out.println("a == b: " + (a == b)); // 预期输出：true
        System.out.println("c == d: " + (c == d)); // 如果设置生效，预期输出：true，否则为false
    }
}

