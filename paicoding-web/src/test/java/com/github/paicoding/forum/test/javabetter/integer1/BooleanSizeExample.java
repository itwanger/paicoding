package com.github.paicoding.forum.test.javabetter.integer1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 6/2/24
 */
public class BooleanSizeExample {
    public static void main(String[] args) {
        boolean singleBoolean = true;
        boolean[] booleanArray = new boolean[10];
        int i = 0;
        byte b = 0;

        // 分析内存占用，可以使用第三方工具如 JOL（Java Object Layout）
        System.out.println("Size of single boolean: " + org.openjdk.jol.info.ClassLayout.parseInstance(singleBoolean).toPrintable());
        System.out.println("Size of boolean array: " + org.openjdk.jol.info.ClassLayout.parseInstance(booleanArray).toPrintable());
        System.out.println("Size of int: " + org.openjdk.jol.info.ClassLayout.parseInstance(i).toPrintable());
        System.out.println("Size of byte: " + org.openjdk.jol.info.ClassLayout.parseInstance(b).toPrintable());
    }
}
