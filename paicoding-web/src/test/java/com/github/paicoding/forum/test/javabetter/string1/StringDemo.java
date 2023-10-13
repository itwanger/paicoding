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
    }
}
