package com.github.paicoding.forum.test.javabetter.array1;

import java.util.Arrays;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/13/23
 */
public class Copy {
    public static void main(String[] args) {
        int[] array1 = {1, 2, 3};
        int[] array2 = {4, 5, 6};

        // 创建一个新数组，长度为两个数组长度之和
        int[] mergedArray = new int[array1.length + array2.length];

        // 复制第一个数组到新数组
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.out.println(Arrays.toString(mergedArray));

        // 复制第二个数组到新数组
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        System.out.println(Arrays.toString(mergedArray));
    }
}
