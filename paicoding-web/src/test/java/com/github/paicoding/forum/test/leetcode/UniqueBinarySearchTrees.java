package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 11/4/23
 */
import java.util.Scanner;

public class UniqueBinarySearchTrees {

    // 使用一个数组来存储已经计算过的结果
    private static int[] cache;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("键入 n 的大小: ");
        int n = scanner.nextInt();

        // 初始化记忆数组，大小为 n+1，所有值默认为 0
        cache = new int[n + 1];

        // 计算并打印结果
        System.out.println("二叉搜索树的数量 n = " + n + " 是: " + numTrees(n));
    }

    public static int numTrees(int n) {
        if (n <= 1) {
            return 1;
        }
        if (cache[n] != 0) {
            return cache[n];
        }

        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += numTrees(i - 1) * numTrees(n - i);
        }

        cache[n] = sum;
        return sum;
    }
}
