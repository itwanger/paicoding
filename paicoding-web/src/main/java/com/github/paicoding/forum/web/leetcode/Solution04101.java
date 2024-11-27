package com.github.paicoding.forum.web.leetcode;

class Solution04101 {
    public int firstMissingPositive(int[] nums) {
        int n = nums.length;

        // 从 1 开始检查每个正整数是否存在于数组中
        for (int i = 1; i <= n + 1; i++) {
            boolean found = false;
            for (int j = 0; j < n; j++) {
                if (nums[j] == i) {
                    found = true;
                    break;
                }
            }
            // 如果当前整数 i 不存在于数组中，返回 i
            if (!found) {
                return i;
            }
        }

        // 理论上不会执行到这里，因为我们在 for 循环中返回了结果
        return n + 1;
    }

    public static void main(String[] args) {
        Solution04101 solution = new Solution04101();
        int[] nums1 = {1, 2, 0};
        int[] nums2 = {3, 4, -1, 1};
        int[] nums3 = {7, 8, 9, 11, 12};

        System.out.println("缺失的第一个正数: " + solution.firstMissingPositive(nums1)); // 输出 3
        System.out.println("缺失的第一个正数: " + solution.firstMissingPositive(nums2)); // 输出 2
        System.out.println("缺失的第一个正数: " + solution.firstMissingPositive(nums3)); // 输出 1
    }
}
