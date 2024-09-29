package com.github.paicoding.forum.web.leetcode;

class Solution05003 {
    public double myPow(double x, int n) {
        // 处理 n 为负数的情况
        long N = n;
        if (N < 0) {
            x = 1 / x;
            N = -N;
        }
        return fastPow(x, N);
    }

    // 递归实现快速幂
    private double fastPow(double x, long n) {
        // 基本情况：当 n 为 0 时，x^0 = 1
        if (n == 0) {
            return 1.0;
        }

        // 递归计算 x^(n/2)，将 n 除以 2 来缩小问题规模
        double half = fastPow(x, n / 2);

        // 如果 n 是偶数，x^n = (x^(n/2)) * (x^(n/2)) = half * half
        if (n % 2 == 0) {
            return half * half;
        }
        // 如果 n 是奇数，x^n = x * (x^(n/2)) * (x^(n/2)) = half * half * x
        else {
            return half * half * x;
        }
    }

    public static void main(String[] args) {
        Solution05003 solution = new Solution05003();
        System.out.println(solution.myPow(2.0, 10));  // 输出: 1024.0
        System.out.println(solution.myPow(2.1, 3));   // 输出: 9.261
        System.out.println(solution.myPow(2.0, -2));  // 输出: 0.25
    }
}
