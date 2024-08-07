package com.github.paicoding.forum.web.leetcode;

public class Solution04203 {
    public int trap(int[] height) {
        // 初始化指针和变量
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int totalWater = 0;

        // 开始双指针向中间移动
        while (left < right) {
            // 如果左边柱子高度小于等于右边
            if (height[left] < height[right]) {
                // 如果当前左边柱子高度大于或等于 leftMax，更新 leftMax
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    // 否则，计算当前左边柱子能接的雨水量
                    totalWater += leftMax - height[left];
                }
                // 移动左指针
                left++;
            } else {
                // 如果当前右边柱子高度大于或等于 rightMax，更新 rightMax
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    // 否则，计算当前右边柱子能接的雨水量
                    totalWater += rightMax - height[right];
                }
                // 移动右指针
                right--;
            }
        }

        return totalWater; // 返回总的接雨水量
    }

    public static void main(String[] args) {
        Solution04203 solution = new Solution04203();
        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        System.out.println("能接的雨水量: " + solution.trap(height)); // 输出 6
    }
}
