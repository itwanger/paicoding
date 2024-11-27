package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.List;

public class Solution03901 {
    // 主方法，找到所有组合，使得组合中数字的和为目标值 target
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        // 初始化结果列表，用于存储所有符合条件的组合
        List<List<Integer>> result = new ArrayList<>();
        // 调用辅助方法，生成所有可能的组合
        generateAllCombinations(candidates, target, new ArrayList<>(), result, 0);
        // 返回结果列表
        return result;
    }

    // 辅助方法，递归生成所有可能的组合
    private void generateAllCombinations(int[] candidates, int target, List<Integer> combination, List<List<Integer>> result, int start) {
        // 计算当前组合的和
        int sum = combination.stream().mapToInt(Integer::intValue).sum();
        // 如果当前组合的和大于目标值，返回（剪枝）
        if (sum > target) {
            return;
        }
        // 如果当前组合的和等于目标值，将当前组合添加到结果列表中
        if (sum == target) {
            result.add(new ArrayList<>(combination));
            return;
        }
        // 循环遍历从 start 到 candidates.length 的每一个元素
        for (int i = start; i < candidates.length; i++) {
            // 将当前元素加入组合列表
            combination.add(candidates[i]);
            // 递归调用辅助方法，继续生成可能的组合
            generateAllCombinations(candidates, target, combination, result, i);
            // 回溯，撤销当前选择，继续尝试下一个元素
            combination.remove(combination.size() - 1);
        }
    }

    // 主程序入口，用于测试
    public static void main(String[] args) {
        Solution03901 solution = new Solution03901();
        // 定义候选数组和目标值
        int[] candidates = {2, 3, 6, 7};
        int target = 7;
        // 调用主方法，找到所有符合条件的组合
        List<List<Integer>> result = solution.combinationSum(candidates, target);
        // 输出结果
        System.out.println("所有组合: " + result);
    }
}
