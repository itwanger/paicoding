package com.github.paicoding.forum.web.leetcode;

public class Solution05302 {
    public int maxSubArray(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n]; // 定义 dp 数组
        dp[0] = nums[0]; // 初始化第一个状态
        int maxSum = dp[0]; // 初始化全局最大值

        // 从第二个元素开始遍历
        for (int i = 1; i < n; i++) {
            // 状态转移方程
            dp[i] = Math.max(dp[i - 1] + nums[i], nums[i]);
            // 更新全局最大值
            maxSum = Math.max(maxSum, dp[i]);
        }

        return maxSum; // 返回最大子数组的和
    }

    public static void main(String[] args) {
        Solution05302 solution = new Solution05302();
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println(solution.maxSubArray(nums)); // 输出: 6
    }
}
