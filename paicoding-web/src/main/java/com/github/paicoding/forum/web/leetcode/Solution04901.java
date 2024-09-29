package com.github.paicoding.forum.web.leetcode;

import java.util.*;

public class Solution04901 {
    public List<List<String>> groupAnagrams(String[] strs) {
        // 创建哈希表，键是排序后的字符串，值是异位词列表
        Map<String, List<String>> map = new HashMap<>();

        // 遍历每一个字符串
        for (String str : strs) {
            // 将字符串转换为字符数组并排序
            char[] charArray = str.toCharArray();
            Arrays.sort(charArray);
            String sortedStr = new String(charArray); // 将排序后的字符数组转回字符串

            // 将排序后的字符串作为键，原始字符串添加到对应的列表中
            if (!map.containsKey(sortedStr)) {
                map.put(sortedStr, new ArrayList<>());
            }
            map.get(sortedStr).add(str); // 添加异位词到列表中
        }

        // 返回哈希表中的所有值，即所有异位词分组
        return new ArrayList<>(map.values());
    }

    public static void main(String[] args) {
        Solution04901 solution = new Solution04901();
        String[] strs = {"eat", "tea", "tan", "ate", "nat", "bat"};
        List<List<String>> result = solution.groupAnagrams(strs);
        System.out.println(result); // 输出 [["eat","tea","ate"], ["tan","nat"], ["bat"]]
    }
}