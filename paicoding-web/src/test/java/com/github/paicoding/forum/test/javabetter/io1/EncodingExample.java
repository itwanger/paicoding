package com.github.paicoding.forum.test.javabetter.io1;

import java.nio.charset.Charset;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/22/23
 */
public class EncodingExample {
    public static void main(String[] args) {
        String str = "沉默王二";
        String charsetName = "UTF-8";

        // 编码
        byte[] bytes = str.getBytes(Charset.forName(charsetName));
        System.out.println("编码: " + bytes);

        // 解码
        String decodedStr = new String(bytes, Charset.forName(charsetName));
        System.out.println("解码: " + decodedStr);
    }
}
