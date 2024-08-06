package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Solution04002 {
    private List<List<Integer>> result = new ArrayList<>();

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates); // 排序以便于跳过重复元素
        backtrack(candidates, target, 0, new ArrayList<>());
        return result;
    }

    private void backtrack(int[] candidates, int target, int start, List<Integer> combination) {
        if (target == 0) {
            result.add(new ArrayList<>(combination));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            // 跳过同一层中重复的元素
            if (i > start && candidates[i] == candidates[i - 1]) {
                continue;
            }

            // 剪枝：当前候选数大于剩余目标值，直接返回
            if (candidates[i] > target) {
                break;
            }

            combination.add(candidates[i]);
            // 递归调用时，下一次递归的起点要从当前元素的下一个元素开始
            backtrack(candidates, target - candidates[i], i + 1, combination);
            combination.remove(combination.size() - 1); // 回溯，撤销最后的选择
        }
    }

    public static void main(String[] args) {
        Solution04002 solution = new Solution04002();
        int[] candidates = {10, 1, 2, 7, 6, 1, 5};
        int target = 8;
        List<List<Integer>> result = solution.combinationSum2(candidates, target);
        System.out.println("所有组合: " + result);
    }
}