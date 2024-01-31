package com.github.paicoding.forum.test.leetcode;

import java.util.LinkedList;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/29/24
 */
class Main01701 {
    public static void main(String[] args) {
        Solution01701 solution = new Solution01701();
        List<String> ans = solution.letterCombinations("234");
        System.out.println(ans);
    }
}

class Solution01701 {
    public List<String> letterCombinations(String digits) {
        LinkedList<String> combinations = new LinkedList<>();
        // 如果输入为空，直接返回空列表
        if (digits == null || digits.length() == 0) {
            return combinations;
        }

        // 数字到字母的映射
        String[] mapping = {"0", "1", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        combinations.add(""); // 初始添加一个空字符串到队列中

        // 遍历每个数字
        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i)); // 将当前字符转换为数字
            // 当队列中的字符串长度与当前处理的数字索引相同时，处理队列中的字符串
            while (combinations.peek().length() == i) {
                String t = combinations.remove(); // 从队列中取出一个字符串
                // 遍历当前数字对应的所有字母
                for (char s : mapping[digit].toCharArray()) {
                    combinations.add(t + s); // 将取出的字符串与当前字母相结合，然后加入队列中
                }
            }
        }
        return combinations; // 返回包含所有组合的列表
    }
}
