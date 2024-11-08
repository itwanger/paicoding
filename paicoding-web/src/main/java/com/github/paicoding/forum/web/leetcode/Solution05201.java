package com.github.paicoding.forum.web.leetcode;

class Solution05201 {
    // 记录解的数量
    int solutions = 0;
    // 解决 N 皇后问题
    public int totalNQueens(int n) {
        // 初始化棋盘
        char[][] board = new char[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = '.';
            }
        }
        // 开始递归查找所有解
        backtrack(board, 0, n);
        return solutions;
    }

    // 回溯方法
    private void backtrack(char[][] board, int row, int n) {
        // 如果已成功放置完所有皇后，将当前棋盘方案加入 solutions
        if (row == n) {
            solutions = solutions + 1;
            return;
        }

        // 尝试在当前行的每一列放置皇后
        for (int col = 0; col < n; col++) {
            // 检查当前位置是否安全
            if (isValid(board, row, col, n)) {
                // 放置皇后
                board[row][col] = 'Q';
                // 递归处理下一行
                backtrack(board, row + 1, n);
                // 回溯，撤销当前放置
                board[row][col] = '.';
            }
        }
    }

    // 检查在 (row, col) 位置放置皇后是否安全
    private boolean isValid(char[][] board, int row, int col, int n) {
        // 检查列上是否有皇后
        for (int i = 0; i < row; i++) {
            if (board[i][col] == 'Q') {
                return false;
            }
        }

        // 检查左上方对角线上是否有皇后
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (board[i][j] == 'Q') {
                return false;
            }
        }

        // 检查右上方对角线上是否有皇后
        for (int i = row - 1, j = col + 1; i >= 0 && j < n; i--, j++) {
            if (board[i][j] == 'Q') {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        Solution05201 solution = new Solution05201();
        System.out.println(solution.totalNQueens(4));
    }
}