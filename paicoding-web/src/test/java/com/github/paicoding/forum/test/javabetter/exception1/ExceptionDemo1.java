package com.github.paicoding.forum.test.javabetter.exception1;

import java.util.Scanner;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/26/23
 */
public class ExceptionDemo1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        try {
            int num = parseInt(str);
            System.out.println("转换结果：" + num);
        } catch (NumberFormatException e) {
            System.out.println("转换失败：" + e.getMessage());
        }
    }

    public static int parseInt(String str) {
        if (str == null || "".equals(str)) {
            throw new NullPointerException("字符串为空");
        }
        if (!str.matches("\\d+")) {
            throw new NumberFormatException("字符串不是数字");
        }
        return Integer.parseInt(str);
    }
}
