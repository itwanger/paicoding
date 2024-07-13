package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/21/23
 */
import java.util.Scanner;

public class WordSearch {

    private static final int[] dirX = {0, 0, 1, -1};
    private static final int[] dirY = {1, -1, 0, 0};
    private static boolean[][] vis;
    private static char[][] map;
    private static String str;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 输入提示语句
        System.out.println("请输入二维数组的行数：");
        int rows = scanner.nextInt();
        System.out.println("请输入二维数组的列数：");
        int cols = scanner.nextInt();
        map = new char[rows][cols];
        System.out.println("请输入二维数组：");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                map[i][j] = scanner.next().charAt(0);
            }
        }
        System.out.println("请输入要查找的字符串：");
        str = scanner.next();
        System.out.println(exist(map, str));
    }

    public static boolean exist(char[][] board, String word) {
        vis = new boolean[board.length][board[0].length];
        map = board;
        str = word;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == word.charAt(0)) {
                    if (DFS(i, j, 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkMove(int posX, int posY) {
        return posX < map.length && posX >= 0 && posY < map[0].length && posY >= 0 && !vis[posX][posY];
    }

    private static boolean DFS(int posX, int posY, int step) {
        if (map[posX][posY] != str.charAt(step)) return false;

        if (step == str.length() - 1) return true;

        vis[posX][posY] = true;
        for (int i = 0; i < 4; i++) {
            int nextX = posX + dirX[i];
            int nextY = posY + dirY[i];
            if (checkMove(nextX, nextY) && DFS(nextX, nextY, step + 1)) {
                return true;
            }
        }
        vis[posX][posY] = false;
        return false;
    }
}
