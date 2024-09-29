package com.github.paicoding.forum.web.leetcode;

public class Solution05002 {
    public double myPow(double x, int n) {
        return Math.pow(x, n);
    }

    public static void main(String[] args) {
        Solution05002 solution = new Solution05002();
        System.out.println(solution.myPow(2.00000, 10));
        System.out.println(solution.myPow(2.10000, 3));
        System.out.println(solution.myPow(2.00000, -2));
    }
}
