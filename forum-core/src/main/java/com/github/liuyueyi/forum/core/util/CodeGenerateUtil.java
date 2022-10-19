package com.github.liuyueyi.forum.core.util;

import java.util.Random;

/**
 * @author YiHui
 * @date 2022/8/15
 */
public class CodeGenerateUtil {

    private static final Random random = new Random();

    public static String genCode() {
        return String.format("%04d", random.nextInt(10000));
    }
}
