package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.List;

class Solution05101 {
    // 解决 N 皇后问题
    public List<List<String>> solveNQueens(int n) {
        List<List<String>> solutions = new ArrayList<>();
        // 初始化棋盘
        char[][] board = new char[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = '.';
            }
        }
        // 开始递归查找所有解
        backtrack(solutions, board, 0, n);
        return solutions;
    }

    // 回溯方法
    private void backtrack(List<List<String>> solutions, char[][] board, int row, int n) {
        // 如果已成功放置完所有皇后，将当前棋盘方案加入 solutions
        if (row == n) {
            solutions.add(construct(board));
            return;
        }

        // 尝试在当前行的每一列放置皇后
        for (int col = 0; col < n; col++) {
            // 检查当前位置是否安全
            if (isValid(board, row, col, n)) {
                // 放置皇后
                board[row][col] = 'Q';
                // 递归处理下一行
                backtrack(solutions, board, row + 1, n);
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

    // 将当前棋盘状态转换为字符串列表
    private List<String> construct(char[][] board) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            result.add(new String(board[i]));
        }
        return result;
    }

    public static void main(String[] args) {
        Solution05101 solution = new Solution05101();
        System.out.println(solution.solveNQueens(4));
    }
}