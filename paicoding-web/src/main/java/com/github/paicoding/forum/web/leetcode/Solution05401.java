package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.List;

class Solution05401 {
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return result;
        }

        int top = 0, bottom = matrix.length - 1; // 上下边界
        int left = 0, right = matrix[0].length - 1; // 左右边界

        while (top <= bottom && left <= right) {
            // 从左到右遍历当前上边界
            for (int i = left; i <= right; i++) {
                result.add(matrix[top][i]);
            }
            top++; // 上边界缩小

            // 从上到下遍历当前右边界
            for (int i = top; i <= bottom; i++) {
                result.add(matrix[i][right]);
            }
            right--; // 右边界缩小

            // 从右到左遍历当前下边界（需要检查上下边界是否相交）
            if (top <= bottom) {
                for (int i = right; i >= left; i--) {
                    result.add(matrix[bottom][i]);
                }
                bottom--; // 下边界缩小
            }

            // 从下到上遍历当前左边界（需要检查左右边界是否相交）
            if (left <= right) {
                for (int i = bottom; i >= top; i--) {
                    result.add(matrix[i][left]);
                }
                left++; // 左边界缩小
            }
        }

        return result;
    }

    public static void main(String[] args) {
        Solution05401 solution = new Solution05401();
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        System.out.println(solution.spiralOrder(matrix)); // 输出: [1, 2, 3, 6, 9, 8, 7, 4, 5]
    }
}