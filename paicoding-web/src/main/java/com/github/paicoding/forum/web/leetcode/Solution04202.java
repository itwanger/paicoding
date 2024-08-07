package com.github.paicoding.forum.web.leetcode;

public class Solution04202 {
    public int trap(int[] height) {
        int n = height.length;
        if (n == 0) return 0;

        // 预先计算每个柱子的左侧最高柱子高度
        int[] leftMax = new int[n];
        leftMax[0] = height[0];
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i - 1], height[i]);
        }

        // 预先计算每个柱子的右侧最高柱子高度
        int[] rightMax = new int[n];
        rightMax[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i + 1], height[i]);
        }

        // 计算每个柱子能接的雨水量
        int totalWater = 0;
        for (int i = 0; i < n; i++) {
            totalWater += Math.min(leftMax[i], rightMax[i]) - height[i];
        }

        return totalWater;
    }

    public static void main(String[] args) {
        Solution04202 solution = new Solution04202();
        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        System.out.println("能接的雨水量: " + solution.trap(height)); // 输出 6
    }
}
