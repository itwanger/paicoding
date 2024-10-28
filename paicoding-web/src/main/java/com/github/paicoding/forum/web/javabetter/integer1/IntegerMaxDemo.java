package com.github.paicoding.forum.web.javabetter.integer1;

public class IntegerMaxDemo {
    public static void main(String[] args) {
        int maxValue = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE = " + maxValue);
        System.out.println("Integer.MAX_VALUE + 1 = " + (maxValue + 1));

        // 用二进制来表示最大值和最小值
        System.out.println("Integer.MAX_VALUE in binary: " + Integer.toBinaryString(maxValue));
        System.out.println("Integer.MIN_VALUE in binary: " + Integer.toBinaryString(Integer.MIN_VALUE));
    }
}
