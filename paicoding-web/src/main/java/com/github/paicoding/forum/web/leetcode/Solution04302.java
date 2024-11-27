package com.github.paicoding.forum.web.leetcode;

public class Solution04302 {
    public String multiply(String num1, String num2) {
        int m = num1.length(), n = num2.length();
        int[] result = new int[m + n]; // 用于存储最终结果的每一位

        // 从右到左遍历 num1 和 num2 的每一位
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                int mul = (num1.charAt(i) - '0') * (num2.charAt(j) - '0'); // 两位数字的乘积
                // 乘积在结果数组中的索引位置
                int p1 = i + j, p2 = i + j + 1;
                int sum = mul + result[p2]; // 当前乘积加上当前位置已有的值

                result[p2] = sum % 10; // 存储个位数
                result[p1] += sum / 10; // 处理进位
            }
        }

        // 将结果数组转换为字符串
        StringBuilder sb = new StringBuilder();
        for (int num : result) {
            if (!(sb.length() == 0 && num == 0)) { // 去掉前导零
                sb.append(num);
            }
        }

        return sb.length() == 0 ? "0" : sb.toString(); // 处理乘积为 0 的情况
    }

    public static void main(String[] args) {
        Solution04302 solution = new Solution04302();
        System.out.println(solution.multiply("2", "3")); // 输出 "6"
        System.out.println(solution.multiply("123", "456")); // 输出 "56088"
    }
}