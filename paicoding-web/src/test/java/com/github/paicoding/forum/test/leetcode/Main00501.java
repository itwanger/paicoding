package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/18/23
 */
import java.util.Scanner;

public class Main00501 {
    public static void main(String[] args) {
        // 请输入字符串
        System.out.println("请输入字符串：");
        // 创建 Scanner 对象用于读取输入
        Scanner scanner = new Scanner(System.in);

        // 读取字符串
        String s = scanner.nextLine();

        // 创建 Solution 实例并调用 longestPalindrome 方法
        Solution00501 solution = new Solution00501();
        String result = solution.longestPalindrome(s);

        // 输出结果
        System.out.println(result);
    }
}

class Solution00501 {
    public String longestPalindrome(String s) {
        String rev = new StringBuffer(s).reverse().toString();
        int n = s.length();
        int[][] f = new int[n][n];
        int maxLen = 1;
        int begPos = 0;
        for(int j = 0;j < n;j++)
            if(rev.charAt(j) == s.charAt(0))
                f[0][j] = 1;
        for(int i = 1;i < n;i++){
            f[i][0] = s.charAt(i) == rev.charAt(0) ? 1 : 0;
            for(int j = 1;j < n;j++){
                if(s.charAt(i) == rev.charAt(j))
                    f[i][j] = f[i - 1][j - 1] + 1;
                if(f[i][j] > maxLen){
                    int befPos = n - j - 1;
                    if(befPos + f[i][j] - 1 == i) {
                        maxLen = f[i][j];
                        begPos = befPos;
                    }
                }
            }
        }
        return s.substring(begPos,begPos + maxLen);
    }
}
