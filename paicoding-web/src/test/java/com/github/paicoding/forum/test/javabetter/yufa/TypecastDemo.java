package com.github.paicoding.forum.test.javabetter.yufa;

import java.util.LinkedList;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/30/24
 */
public class TypecastDemo {
    public static void main(String[] args) {
        int a = 1500000000, b = 1500000000;
        int sum = a + b;
        long sum1 = a + b;
        long sum2 = (long)a + b;
        long sum3 = (long)(a + b);

        System.out.println(sum);
        System.out.println(sum1);
        System.out.println(sum2);
        System.out.println(sum3);

        System.out.println(sum2 - 2147483648L);
        System.out.println(sum2 - 1294967296);

        LinkedList<String> list = new LinkedList<>();
    }
}
