package com.github.paicoding.forum.web.leetcode;

import java.util.Scanner;

public class Main0322 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 输入数组长度 n
        int n = scanner.nextInt();
        int[] a = new int[n];

        // 输入数组元素
        for (int i = 0; i < n; i++) {
            a[i] = scanner.nextInt();
        }

        // 初始化变量
        long sumMex = 0; // 所有子数组的 mex 值之和
        int last_0 = -1, last_1 = -1; // 最近一次出现 0 和 1 的位置

        // 遍历数组
        for (int i = 0; i < n; i++) {
            // 更新最近一次出现 0 和 1 的位置
            if (a[i] == 0) {
                last_0 = i;
            } else if (a[i] == 1) {
                last_1 = i;
            }

            // 计算以 a[i] 结尾的所有子数组的 mex 值
            if (last_0 == -1) {
                // 没有 0，所有子数组的 mex = 0
                sumMex += 0;
            } else if (last_1 == -1) {
                // 没有 1，所有子数组的 mex = 1
                sumMex += (i - last_0 + 1);
            } else {
                // 同时有 0 和 1，所有子数组的 mex = 2
                sumMex += (i - Math.min(last_0, last_1) + 1);
            }
        }

        // 输出结果
        System.out.println(sumMex);
    }
}
