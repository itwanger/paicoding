package com.github.paicoding.forum.test.javabetter.importance;

import java.nio.charset.StandardCharsets;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/8/23
 */
public class EncodingDemo1 {
    public static void main(String[] args) {
        String originalStr = "沉默王二";

        byte[] bytes = originalStr.getBytes(StandardCharsets.UTF_8);
        String encodedStr = new String(bytes, StandardCharsets.UTF_8);

        System.out.println("正确转码前: " + originalStr);
        System.out.println("正确转码后: " + encodedStr);
    }
}
