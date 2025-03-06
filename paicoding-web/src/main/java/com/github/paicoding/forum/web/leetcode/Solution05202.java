package com.github.paicoding.forum.web.leetcode;

class Solution05202 {
    // 返回 N 皇后问题的解的数量
    public int totalNQueens(int n) {
        // 初始化列、主对角线、副对角线的布尔数组
        boolean[] cols = new boolean[n];         // 列
        boolean[] diag1 = new boolean[2 * n - 1]; // 主对角线
        boolean[] diag2 = new boolean[2 * n - 1]; // 副对角线

        // 使用递归回溯来计数
        return backtrack(0, n, cols, diag1, diag2);
    }

    // 回溯，返回从当前行开始的解的数量
    private int backtrack(int row, int n, boolean[] cols, boolean[] diag1, boolean[] diag2) {
        // 如果所有行都放置完毕，说明找到一种有效解，返回 1
        if (row == n) {
            return 1;
        }

        int solutions = 0; // 记录解的数量

        // 遍历当前行的每一列，尝试放置皇后
        for (int col = 0; col < n; col++) {
            // 计算当前列、主对角线、副对角线的索引
            int d1 = row - col + n - 1; // 主对角线索引
            int d2 = row + col;         // 副对角线索引

            // 检查当前位置是否被占用
            if (cols[col] || diag1[d1] || diag2[d2]) {
                continue; // 如果当前列或对角线被占用，跳过该位置
            }

            // 放置皇后，标记当前列和对角线已被占用
            cols[col] = true;
            diag1[d1] = true;
            diag2[d2] = true;

            // 递归处理下一行，并累加解的数量
            solutions += backtrack(row + 1, n, cols, diag1, diag2);

            // 回溯，撤销当前皇后位置的占用状态
            cols[col] = false;
            diag1[d1] = false;
            diag2[d2] = false;
        }

        return solutions; // 返回当前分支的解的数量
    }

    public static void main(String[] args) {
        Solution05202 solution = new Solution05202();
        System.out.println(solution.totalNQueens(4)); // 输出: 2
    }
}