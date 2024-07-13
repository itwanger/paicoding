package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.HashSet;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/25/24
 */
public class HashSetDemo {
    public static void main(String[] args) {
        // 创建一个 HashSet 对象
        HashSet<String> set = new HashSet<>();

// 添加元素
        set.add("沉默");
        set.add("王二");
        set.add("陈清扬");
        set.add("沉默");

// 输出 HashSet 的元素个数
        System.out.println("HashSet size: " + set.size()); // output: 3

// 遍历 HashSet
        for (String s : set) {
            System.out.println(s);
        }
    }
}
