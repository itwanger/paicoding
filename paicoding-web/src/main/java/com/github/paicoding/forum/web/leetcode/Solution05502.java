package com.github.paicoding.forum.web.leetcode;

class Solution05502 {
    public boolean canJump(int[] nums) {
        int maxReach = 0; // 当前能跳到的最远位置

        for (int i = 0; i < nums.length; i++) {
            // 如果当前位置超出了最远可达范围，直接返回 false
            if (i > maxReach) {
                return false;
            }
            // 更新最远可达位置
            maxReach = Math.max(maxReach, i + nums[i]);
            // 如果最远可达位置已经超过或等于最后一个位置，返回 true
            if (maxReach >= nums.length - 1) {
                return true;
            }
        }

        return false; // 遍历结束后未到达最后一个位置
    }

    public static void main(String[] args) {
        Solution05502 solution = new Solution05502();
        System.out.println(solution.canJump(new int[]{2, 3, 1, 1, 4})); // 输出: true
        System.out.println(solution.canJump(new int[]{3, 2, 1, 0, 4})); // 输出: false
    }
}
