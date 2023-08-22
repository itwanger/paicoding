package com.github.paicoding.forum.test.javabetter.thread1;

import cn.hutool.core.lang.UUID;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/17/23
 */
public class ConcurrentHashMapDemo {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        map.put("e", 5);
        map.put("f", 6);
        map.put("g", 7);
        map.put("h", 8);
        map.put("i", 9);
        map.put("j", 10);
        map.put("k", 11);
        map.put("l", 12);
        map.put("m", 13);
        map.put("n", 14);
        map.put("o", 15);
        map.put("p", 16);
        map.put("q", 17);
        map.put("r", 18);
        map.put("s", 19);
        map.put("t", 20);
        map.put("u", 21);
        map.put("v", 22);
        map.put("w", 23);
        map.put("x", 24);
        map.put("y", 25);
        map.put("z", 26);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                map.put(UUID.randomUUID().toString().substring(0, 1), new Random().nextInt(10));
                System.out.println(map);
            }, String.valueOf(i)).start();
        }
    }
}