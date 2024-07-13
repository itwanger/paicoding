package com.github.paicoding.forum.test.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/3/24
 */
public class Main02201 {
    public static void main(String[] args) {
        Solution02201 solution = new Solution02201();
        List<String> result = solution.generateParenthesis(2);
        System.out.println(result);
    }
}

class Solution02201 {
    List<String> combinations = new ArrayList<>();
    // 主方法，调用此函数生成所有的括号组合
    public List<String> generateParenthesis(int n) {
        backtrack("", 0, 0, n);
        return combinations;
    }

    // 回溯方法
    private void backtrack(String current, int open, int close, int max) {
        // 如果当前字符串长度等于最大长度的两倍，说明找到了一个解，添加到结果列表中
        if (current.length() == max * 2) {
            combinations.add(current);
            return;
        }

        // 如果左括号数量小于 n，可以添加一个左括号
        if (open < max) {
            backtrack(current + "(", open + 1, close, max);
        }
        // 如果右括号数量小于左括号数量，可以添加一个右括号
        if (close < open) {
            backtrack(current + ")", open, close + 1, max);
        }

        System.out.println("current: " + current + ", open: " + open + ", close: " + close + ", max: " + max);
    }
}
