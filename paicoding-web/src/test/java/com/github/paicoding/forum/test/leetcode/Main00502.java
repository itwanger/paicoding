package com.github.paicoding.forum.test.leetcode;

import java.util.Scanner;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/18/23
 */
public class Main00502 {
    public static void main(String[] args) {
        // 请输入字符串
        System.out.println("请输入字符串：");
        // 创建 Scanner 对象用于读取输入
        Scanner scanner = new Scanner(System.in);

        // 读取字符串
        String s = scanner.nextLine();

        // 创建 Solution 实例并调用 longestPalindrome 方法
        Solution00502 solution = new Solution00502();
        String result = solution.longestPalindrome(s);

        // 输出结果
        System.out.println(result);
    }
}


class Solution00502 {
    public String longestPalindrome(String s) {
        int n = s.length();
        boolean[][] dp = new boolean[n][n]; // 创建动态规划表
        String res = ""; // 用于存储最长回文子串

        for (int len = 0; len < n; len++) { // len 表示检查的子串长度
            for (int start = 0; start + len < n; start++) { // start 表示子串的开始位置
                int end = start + len; // end 表示子串的结束位置

                // 对于长度为 1 和 2 的子串，只需比较两端字符是否相等
                if (len == 0) {
                    dp[start][end] = true;
                } else if (len == 1) {
                    dp[start][end] = s.charAt(start) == s.charAt(end);
                } else {
                    // 对于长度大于 2 的子串，除了比较两端字符外，还需要检查内部子串是否为回文
                    dp[start][end] = (s.charAt(start) == s.charAt(end)) && dp[start + 1][end - 1];
                }

                // 更新最长回文子串
                if (dp[start][end] && len + 1 > res.length()) {
                    res = s.substring(start, end + 1);
                }
            }
        }
        return res;
    }
}