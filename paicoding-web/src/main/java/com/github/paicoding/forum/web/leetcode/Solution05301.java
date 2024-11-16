package com.github.paicoding.forum.web.leetcode;

public class Solution05301 {
    public int maxSubArray(int[] nums) {
        int n = nums.length;
        int maxSum = Integer.MIN_VALUE; // 初始化为最小值

        // 枚举每一个子数组
        for (int i = 0; i < n; i++) {
            int currentSum = 0; // 当前子数组的和
            for (int j = i; j < n; j++) {
                currentSum += nums[j]; // 累加当前子数组的元素
                maxSum = Math.max(maxSum, currentSum); // 更新最大和
            }
        }
        return maxSum; // 返回最大子数组的和
    }

    public static void main(String[] args) {
        Solution05301 solution = new Solution05301();
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println(solution.maxSubArray(nums)); // 输出: 6
    }
}
