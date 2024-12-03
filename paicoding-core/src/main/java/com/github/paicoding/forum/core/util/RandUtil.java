package com.github.paicoding.forum.core.util;


import java.util.Random;

/**
 * 随机工具类
 *
 * @author YiHui
 * @date 2024/9/7
 */
public class RandUtil {
    private static Random random = new Random();
    private static final String txt = "0123456789qwertyuiopasdfghjklzxcvbnm";

    public static String random(int len) {
        StringBuilder builder = new StringBuilder();
        int size = txt.length();
        for (int i = 0; i < len; i++) {
            builder.append(txt.charAt(random.nextInt(size)));
        }
        return builder.toString();
    }

}
