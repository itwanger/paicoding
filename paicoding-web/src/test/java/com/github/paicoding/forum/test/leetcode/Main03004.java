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
public class Main03004 {
    public static void main(String[] args) {
        Solution03004 solution = new Solution03004();
        String s = "barfoobarfoothefoobarman";
        String[] words = {"foo", "bar"};
        List<Integer> result = solution.findSubstring(s, words);
        System.out.println(result);
    }
}

class Solution03004 {
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        if (s == null || words == null || words.length == 0) return result;

        // 创建一个HashMap来存储words数组中每个单词的出现次数
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        // 单个单词的长度和窗口的长度（所有单词的总长度）
        int wordLength = words[0].length();
        int windowLength = words.length * wordLength;

        // 遍历字符串s，每次增加的步长为一个单词的长度
        for (int i = 0; i < wordLength; ++i) {
            for (int j = i; j <= s.length() - windowLength; j += wordLength) {
                // 用于记录当前窗口中单词的出现情况
                Map<String, Integer> seenWords = new HashMap<>();
                int start = j;
                int end = j + windowLength;

                // 在窗口内部，每次也是按照单词的长度来切分和检查
                while (start < end) {
                    String currentWord = s.substring(start, start + wordLength);
                    seenWords.put(currentWord, seenWords.getOrDefault(currentWord, 0) + 1);

                    // 如果当前单词不在words中，或者出现的次数超过了words中该单词的次数，则跳出循环
                    if (!wordCount.containsKey(currentWord) || seenWords.get(currentWord) > wordCount.get(currentWord)) {
                        break;
                    }
                    start += wordLength;
                }

                // 如果窗口内的单词正好匹配words数组中的所有单词，则添加到结果中
                if (start == end) {
                    result.add(j);
                }
            }
        }

        return result;
    }


}
