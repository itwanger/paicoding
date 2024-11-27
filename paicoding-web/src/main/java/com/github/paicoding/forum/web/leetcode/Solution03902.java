package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Solution03902 {
    private List<List<Integer>> result = new ArrayList<>();

    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        Arrays.sort(candidates); // 排序有助于提前终止循环
        backtrack(candidates, target, 0, new ArrayList<>());
        return result;
    }

    private void backtrack(int[] candidates, int target, int start, List<Integer> combination) {
        if (target == 0) {
            result.add(new ArrayList<>(combination));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > target) {
                break; // 当前候选数大于剩余目标值，剪枝
            }
            combination.add(candidates[i]);
            backtrack(candidates, target - candidates[i], i, combination); // 因为可以重复使用当前数字，所以传入 i 而不是 i + 1
            combination.remove(combination.size() - 1); // 回溯，撤销最后的选择
        }
    }

    public static void main(String[] args) {
        Solution03902 solution = new Solution03902();
        int[] candidates = {2, 3, 6, 7};
        int target = 7;
        List<List<Integer>> result = solution.combinationSum(candidates, target);
        System.out.println("所有组合: " + result);
    }
}
