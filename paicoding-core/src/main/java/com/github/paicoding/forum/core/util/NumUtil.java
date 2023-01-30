package com.github.paicoding.forum.core.util;

/**
 * @author YiHui
 * @date 2022/8/31
 */
public class NumUtil {

    public static boolean nullOrZero(Long num) {
        return num == null || num == 0L;
    }

    public static boolean nullOrZero(Integer num) {
        return num == null || num == 0;
    }

    public static boolean upZero(Long num) {
        return num != null && num > 0;
    }

    public static boolean upZero(Integer num) {
        return num != null && num > 0;
    }
}
