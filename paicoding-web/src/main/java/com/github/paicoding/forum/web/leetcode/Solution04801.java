package com.github.paicoding.forum.web.leetcode;

import java.util.Arrays;

public class Solution04801 {
    public void rotate(int[][] matrix) {
        int n = matrix.length; // 获取矩阵的维度
        int[][] matrix_new = new int[n][n]; // 创建一个新矩阵来保存原矩阵的副本

        // 复制原矩阵的每一行到新的矩阵
        for (int i = 0; i < n; i++) {
            matrix_new[i] = Arrays.copyOf(matrix[i], n); // 复制每一行
        }

        // 旋转矩阵：将 matrix_new 中的元素重新放入 matrix 中
        for (int i = 0; i < n; i++) { // 遍历原矩阵的每一行
            for (int j = 0; j < n; j++) { // 遍历原矩阵的每一列
                // 将 matrix_new[i][j] 旋转到 matrix[j][n - 1 - i]
                matrix[j][n - 1 - i] = matrix_new[i][j];
            }
        }
    }

    public static void main(String[] args) {
        Solution04801 solution = new Solution04801();
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        solution.rotate(matrix);
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
        // 输出:
        // 7 4 1
        // 8 5 2
        // 9 6 3
    }
}
