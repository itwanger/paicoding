package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/18/23
 */
public class Main00503 {
    public static void main(String[] args) {
        // 请输入字符串
        System.out.println("请输入字符串：");
        // 创建 Scanner 对象用于读取输入
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        // 读取字符串
        String s = scanner.nextLine();

        // 创建 Solution 实例并调用 longestPalindrome 方法
        Solution00503 solution = new Solution00503();
        String result = solution.longestPalindrome(s);

        // 输出结果
        System.out.println(result);
    }
}
class Solution00503 {
    public String longestPalindrome(String s) {
        if (s == null || s.length() < 1) return "";

        int start = 0; // 最长回文子串的起始位置
        int end = 0;   // 最长回文子串的结束位置
        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i); // 以单个字符为中心的回文长度
            int len2 = expandAroundCenter(s, i, i + 1); // 以两个字符之间为中心的回文长度
            int len = Math.max(len1, len2); // 当前找到的最长回文长度
            if (len > end - start) { // 更新最长回文子串的位置
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }
        return s.substring(start, end + 1); // 返回最长回文子串
    }

    // 中心扩展函数
    private int expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;  // 向左扩展
            right++; // 向右扩展
        }
        return right - left - 1; // 返回扩展后的回文长度
    }
}