package com.github.paicoding.forum.test.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/27/24
 */
public class Main03002 {
    public static void main(String[] args) {
        Solution03002 solution = new Solution03002();
        String s = "barfoobarfoothefoobarman";
        String[] words = {"foo", "bar"};
        List<Integer> result = solution.findSubstring(s, words);
        System.out.println(result);
    }
}

class Solution03002 {
    public List<Integer> findSubstring(String s, String[] words) {
        // 这是 LeetCode 的第 30 题
        List<Integer> result = new ArrayList<>();
        // 生成所有 words 的排列
        PermutationsGenerator generator = new PermutationsGenerator();
        List<String> permutations = generator.generatePermutations(words);
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
}


class PermutationsGenerator {

    // 生成所有排列的方法
    public List<String> generatePermutations(String[] words) {
        List<String> results = new ArrayList<>();
        permute(words, 0, results);
        return results;
    }

    // 辅助方法：递归地生成排列
    private void permute(String[] array, int start, List<String> result) {
        if (start >= array.length) {
            // 将当前排列转换为字符串并添加到结果列表
            result.add(String.join("", array));
        } else {
            for (int i = start; i < array.length; i++) {
                swap(array, start, i); // 交换元素
                permute(array, start + 1, result); // 递归地生成剩余元素的排列
                swap(array, start, i); // 撤销交换
            }
        }
    }

    // 交换数组中的两个元素
    private void swap(String[] array, int i, int j) {
        String temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void main(String[] args) {
        PermutationsGenerator generator = new PermutationsGenerator();
        String[] words = {"a", "b", "c"};
        List<String> permutations = generator.generatePermutations(words);
        System.out.println("排列组合: " + permutations);
    }
}