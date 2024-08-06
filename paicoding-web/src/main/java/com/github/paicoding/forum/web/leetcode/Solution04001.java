package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Solution04001 {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates); // 排序以便于后续处理
        generateAllCombinations(candidates, target, new ArrayList<>(), result, 0);
        return result;
    }

    private void generateAllCombinations(int[] candidates, int target, List<Integer> combination, List<List<Integer>> result, int start) {
        int sum = combination.stream().mapToInt(Integer::intValue).sum();
        if (sum > target) {
            return;
        }
        if (sum == target) {
            if (!result.contains(combination)) {
                result.add(new ArrayList<>(combination)); // 创建组合的副本并添加到结果列表
            }
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            combination.add(candidates[i]);
            generateAllCombinations(candidates, target, combination, result, i + 1); // 每个数字只能使用一次，所以递归从 i+1 开始
            combination.remove(combination.size() - 1); // 回溯，撤销最后的选择
        }
    }

    public static void main(String[] args) {
        Solution04001 solution = new Solution04001();
        int[] candidates = {10, 1, 2, 7, 6, 1, 5};
        int target = 8;
        List<List<Integer>> result = solution.combinationSum2(candidates, target);
        System.out.println("所有组合: " + result);
    }
}