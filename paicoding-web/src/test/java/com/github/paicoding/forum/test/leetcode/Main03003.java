package com.github.paicoding.forum.test.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/27/24
 */
public class Main03003 {
    public static void main(String[] args) {
        Solution03003 solution = new Solution03003();
        String s = "barfoobarfoothefoobarman";
        String[] words = {"foo", "bar"};
        List<Integer> result = solution.findSubstring(s, words);
        System.out.println(result);
    }
}


class Solution03003 {
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> res = new ArrayList<>();
        if (s == null || words == null || s.length() == 0 || words.length == 0) return res;

        int wordLen = words[0].length(); // 单词的长度
        int wordCount = words.length; // 单词的数量

        // 统计words中每个单词应该出现的次数
        Map<String, Integer> wordMap = new HashMap<>();
        for (String word : words) {
            wordMap.put(word, wordMap.getOrDefault(word, 0) + 1);
        }

        // 遍历所有的可能起始位置，0 到 wordLen - 1
        for (int i = 0; i < wordLen; i++) {
            int left = i; // 窗口左边界
            int count = 0; // 窗口中符合条件的单词数量
            Map<String, Integer> windowMap = new HashMap<>(); // 窗口中单词的计数

            // 移动窗口的右边界
            for (int j = i; j <= s.length() - wordLen; j += wordLen) {
                String word = s.substring(j, j + wordLen);

                // 如果word是有效的
                if (wordMap.containsKey(word)) {
                    windowMap.put(word, windowMap.getOrDefault(word, 0) + 1);
                    count++;

                    // 如果窗口中该单词的数量超过了应有的数量，需要调整窗口的左边界
                    while (windowMap.get(word) > wordMap.get(word)) {
                        String leftWord = s.substring(left, left + wordLen);
                        windowMap.put(leftWord, windowMap.get(leftWord) - 1);
                        count--;
                        left += wordLen;
                    }

                    // 如果窗口中的单词数量正好等于words中的单词数量，记录起始位置
                    if (count == wordCount) {
                        res.add(left);
                        // 移动左边界以寻找新的匹配
                        String leftWord = s.substring(left, left + wordLen);
                        windowMap.put(leftWord, windowMap.get(leftWord) - 1);
                        count--;
                        left += wordLen;
                    }
                } else {
                    // 如果word不是有效的，则重置窗口
                    windowMap.clear();
                    count = 0;
                    left = j + wordLen;
                }
            }
        }
        return res;
    }
}