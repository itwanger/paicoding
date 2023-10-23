package com.github.paicoding.forum.test.javabetter.string1;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/9/23
 */
public class StringDemo {
    public static void main(String[] args) {
        String text = "沉默王二";
        System.out.println(Arrays.toString(text.getBytes(StandardCharsets.UTF_8)));

        System.out.println("  沉默王二   ".trim());


        String str = "Hello, world!";
        String prefix = str.substring(0, 5);  // 提取前5个字符，即 "Hello,"
        String suffix = str.substring(7);     // 提取从第7个字符开始的所有字符，即 "world!"

        System.out.println(prefix);
        System.out.println(suffix);
    }
}
