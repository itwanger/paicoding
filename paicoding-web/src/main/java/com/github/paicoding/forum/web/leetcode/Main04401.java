package com.github.paicoding.forum.web.leetcode;

public class Main04401 {
    public boolean isMatch(String s, String p) {
        int m = s.length(), n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];

        // 初始化 dp[0][0]，表示空字符串和空模式匹配
        dp[0][0] = true;

        // 初始化第一行：只有当 p 的前 j 个字符全为 '*' 时，dp[0][j] 为 true
        for (int j = 1; j <= n; j++) {
            if (p.charAt(j - 1) == '*') {
                dp[0][j] = dp[0][j - 1];
            }
        }

        // 填充 dp 表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (p.charAt(j - 1) == s.charAt(i - 1) || p.charAt(j - 1) == '?') {
                    dp[i][j] = dp[i - 1][j - 1]; // 匹配当前字符
                } else if (p.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i - 1][j] || dp[i][j - 1]; // * 匹配一个或多个字符
                }
            }
        }

        // 最终结果
        return dp[m][n];
    }

    public static void main(String[] args) {
        Main04401 solution = new Main04401();
        System.out.println(solution.isMatch("aa", "a")); // 输出 false
        System.out.println(solution.isMatch("aa", "*")); // 输出 true
        System.out.println(solution.isMatch("cb", "?a")); // 输出 false
        System.out.println(solution.isMatch("adceb", "*a*b")); // 输出 true
        System.out.println(solution.isMatch("acdcb", "a*c?b")); // 输出 false
    }
}