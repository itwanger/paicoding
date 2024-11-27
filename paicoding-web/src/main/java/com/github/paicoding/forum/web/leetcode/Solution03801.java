package com.github.paicoding.forum.web.leetcode;

class Solution03801 {
    public String countAndSay(int n) {
        // 初始项是 "1"
        String result = "1";

        // 逐步生成每一项，直到第 n 项
        for (int i = 1; i < n; i++) {
            result = getNextSequence(result);
        }

        return result;
    }

    // 生成外观数列的下一项
    private String getNextSequence(String sequence) {
        StringBuilder nextSequence = new StringBuilder();

        int length = sequence.length();
        int count = 1; // 记录当前字符的出现次数

        for (int i = 1; i < length; i++) {
            if (sequence.charAt(i) == sequence.charAt(i - 1)) {
                count++; // 如果当前字符与前一个字符相同，计数加一
            } else {
                // 如果当前字符与前一个字符不同，将前一个字符和计数添加到下一项中
                nextSequence.append(count).append(sequence.charAt(i - 1));
                count = 1; // 重置计数
            }
        }

        // 处理最后一个字符
        nextSequence.append(count).append(sequence.charAt(length - 1));

        return nextSequence.toString();
    }

    public static void main(String[] args) {
        Solution03801 solution = new Solution03801();
        int n = 5;
        String result = solution.countAndSay(n);
        System.out.println("外观数列的第 " + n + " 项: " + result);
    }
}