package com.github.paicoding.forum.test.leetcode;

import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * 给定一个字符串 s 和一个字符串数组 words。 words 中所有字符串 长度相同。
 *
 *  s 中的 串联子串 是指一个包含  words 中所有字符串以任意顺序排列连接起来的子串。
 *
 *  例如，如果 words = ["ab","cd","ef"]， 那么 "abcdef"， "abefcd"，"cdabef"， "cdefab"，"efabcd"， 和 "efcdab" 都是串联子串。 "acdbef" 不是串联子串，因为他不是任何 words 排列的连接。
 * 返回所有串联子串在 s 中的开始索引。你可以以 任意顺序 返回答案。
 *
 * @author 沉默王二
 * @date 2/27/24
 */
public class Main03001 {
    public static void main(String[] args) {
        Solution03001 solution = new Solution03001();
        String s = "barfoobarfoothefoobarman";
        String[] words = {"foo", "bar"};
        List<Integer> result = solution.findSubstring(s, words);
        System.out.println(result);
    }

}

class Solution03001 {
    public List<Integer> findSubstring(String s, String[] words) {
        // 这是 LeetCode 的第 30 题
        List<Integer> result = new ArrayList<>();
        // 生成所有 words 的排列
        List<String> permutations = generatePermutations(words);
        // 检查每个排列是否为 s 的子串
        for (String permutation : permutations) {
            int index = s.indexOf(permutation);
            while (index != -1) {
                // 如果找到，添加到结果列表中
                if (!result.contains(index)) {
                    result.add(index);
                }
                // 继续查找下一个匹配的子串
                index = s.indexOf(permutation, index + 1);
            }
        }

        return result;
    }

    // 生成 words 的所有排列
    private List<String> generatePermutations(String[] words) {
        List<String> permutations = new ArrayList<>();

        // 把数组转成集合
        List<String> list = new ArrayList<>();
        Collections.addAll(list, words);

        // 生成所有排列
        for (List<String> permutation : Collections2.permutations(list)) {
            StringBuilder sb = new StringBuilder();
            for (String word : permutation) {
                sb.append(word);
            }
            permutations.add(sb.toString());
        }

        return permutations;
    }
}