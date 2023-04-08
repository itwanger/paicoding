package com.github.paicoding.forum.test.javabetter.importance;

import cn.hutool.core.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/8/23
 */
public class EncodingDemo {
    public static void main(String[] args) {
        String originalStr = "沉默王二";
        String encodedStr = "";

        try {
            byte[] bytes = originalStr.getBytes("GBK");
            encodedStr = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("转码前: " + originalStr);
        System.out.println("转码后: " + encodedStr);

        // 输出 efbfbdefbfbd
        char[] kuijinkao = HexUtil.encodeHex("��", StandardCharsets.UTF_8);
        System.out.println(kuijinkao);
        // 借助 hutool 转成二进制
        byte[] testBytes = HexUtil.decodeHex(kuijinkao);
        // 使用 GBK 解码
        String testResult = new String(testBytes, Charset.forName("GBK"));
        // 输出锟斤拷
        System.out.println(testResult);
    }
}
