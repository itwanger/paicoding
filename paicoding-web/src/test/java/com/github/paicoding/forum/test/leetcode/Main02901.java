package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/22/24
 */
public class Main02901 {
    public static void main(String[] args) {
        Solution02901 solution = new Solution02901();
        System.out.println(solution.divide(10, 3));
        System.out.println(solution.divide(7, -3));
        System.out.println(solution.divide(0, 1));
        System.out.println(solution.divide(1, 1));
        System.out.println(solution.divide(1, 0));
        System.out.println(solution.divide(-2147483648, -1));
    }
}


class Solution02901 {
    public int divide(int dividend, int divisor) {
        // 处理特殊情况
        if (dividend == Integer.MIN_VALUE && divisor == -1) {
            return Integer.MAX_VALUE;
        }

        // 符号处理
        boolean negative = (dividend < 0) ^ (divisor < 0);
        long ldividend = Math.abs((long)dividend);
        long ldivisor = Math.abs((long)divisor);

        long result = 0;
        while (ldividend >= ldivisor) {
            ldividend -= ldivisor;
            result++;
        }

        return negative ? (int)-result : (int)result;
    }
}