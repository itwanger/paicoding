package com.github.paicoding.forum.web.leetcode;

import java.math.BigInteger;

public class Solution04301 {
    public String multiply(String num1, String num2) {
        BigInteger n1 = new BigInteger(num1);
        BigInteger n2 = new BigInteger(num2);
        return n1.multiply(n2).toString();
    }

    public static void main(String[] args) {
        Solution04301 solution = new Solution04301();
        String num1 = "123";
        String num2 = "456";
        System.out.println(solution.multiply(num1, num2));
    }
}
