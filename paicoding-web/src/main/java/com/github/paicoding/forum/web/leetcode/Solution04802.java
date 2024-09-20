package com.github.paicoding.forum.web.leetcode;

public class Solution04802 {
    public void rotate(int[][] matrix) {
        int n = matrix.length;

        // 逐层旋转
        for (int layer = 0; layer < n / 2; layer++) {
            int first = layer; // 当前层的起始索引
            int last = n - 1 - layer; // 当前层的结束索引

            for (int i = first; i < last; i++) {
                int offset = i - first; // 偏移量，用来计算位置
                // 保存上边
                int top = matrix[first][i];

                // 左边 -> 上边
                matrix[first][i] = matrix[last - offset][first];

                // 下边 -> 左边
                matrix[last - offset][first] = matrix[last][last - offset];

                // 右边 -> 下边
                matrix[last][last - offset] = matrix[i][last];

                // 上边 -> 右边
                matrix[i][last] = top;
            }
        }
    }

    public static void main(String[] args) {
        Solution04802 solution = new Solution04802();
        int[][] matrix = {
                { 1,  2,  3,  4},
                { 5,  6,  7,  8},
                { 9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        solution.rotate(matrix);
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
        // 输出：
        // 13  9  5  1
        // 14 10  6  2
        // 15 11  7  3
        // 16 12  8  4
    }
}