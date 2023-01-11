package com.github.liuyueyi.forum.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YiHui
 * @date 2022/8/15
 */
public class CodeGenerateUtil {

    private static final Random random = new Random();

    private static final List<String> specialCodes = Arrays.asList(
            "666", "888", "000", "999", "555", "222", "333", "111", "777",
            "520", "110", "119", "911",
            "123", "234", "345", "456", "567", "678", "789"
    );

    public static String genCode(int cnt) {
        if (cnt >= specialCodes.size()) {
            return String.format("%03d", random.nextInt(1000));
        } else {
            return specialCodes.get(cnt);
        }
    }
}
