package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.List;

public class Main04601 {

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        boolean[] used = new boolean[nums.length]; // 标记数字是否被使用
        backtrack(nums, result, path, used);
        return result;
    }

    // 回溯算法
    private void backtrack(int[] nums, List<List<Integer>> result, List<Integer> path, boolean[] used) {
        // 如果当前排列的长度等于 nums 的长度，则找到一个完整的排列
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path)); // 将当前排列加入结果列表
            return;
        }

        // 遍历每一个数字，尝试将其加入排列
        for (int i = 0; i < nums.length; i++) {
            // 如果该数字已经使用过，则跳过
            if (used[i]) {
                continue;
            }

            // 做选择：选择当前数字
            path.add(nums[i]);
            used[i] = true;

            // 继续递归处理剩余数字的排列
            backtrack(nums, result, path, used);

            // 撤销选择：回溯
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }

    public static void main(String[] args) {
        Main04601 solution = new Main04601();
        int[] nums = {1, 2, 3};
        List<List<Integer>> result = solution.permute(nums);
        System.out.println(result); // 输出所有全排列
    }
}
