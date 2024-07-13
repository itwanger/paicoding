package com.github.paicoding.forum.test.ai;

import java.util.Arrays;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/14/23
 */
public class TestWenxin {
    public static void main(String[] args) {
        // 写一段冒泡排序
        int[] arr = {3, 5, 2, 8, 7, 1, 4};
        bubbleSortCopilot(arr);
        System.out.println("copilot " + Arrays.toString(arr));

        int[] arr1 = {3, 5, 2, 8, 7, 1, 4};
        bubbleSort(arr1);
        System.out.println("qianfan " + Arrays.toString(arr1)); // [1, 2, 3, 4, 5, 7, 8]

    }

    public static void bubbleSortCopilot(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            boolean flag = false;
            for (int j = 0; j < arr.length - i - 1; j++) {
                int temp;
                if (arr[j] > arr[j + 1]) {
                    flag = true;
                    temp = arr[j + 1];
                    arr[j + 1] = arr[j];
                    arr[j] = temp;
                }
            }
            if (!flag) {
                break;
            }
        }
    }

    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
// 交换元素位置
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}
