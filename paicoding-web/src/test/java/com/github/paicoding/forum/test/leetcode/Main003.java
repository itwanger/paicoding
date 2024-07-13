package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/12/23
 */
import java.util.Scanner;

public class Main003 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入字符串: ");
        String s = scanner.nextLine(); // 从标准输入读取字符串

        Solution solution = new Solution();
        int length = solution.lengthOfLongestSubstring(s); // 调用解题函数

        System.out.println("没有重复的最长子串为: " + length); // 输出结果
    }
}

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int res = 0;
        for (int i = 0; i < s.length(); i++) {
            boolean[] book = new boolean[128]; // ASCII 字符集
            for (int j = i; j >= 0; j--) {
                if (book[s.charAt(j)]) {
                    break;
                }
                book[s.charAt(j)] = true;
                res = Math.max(res, i - j + 1);
            }
        }
        return res;
    }
}
