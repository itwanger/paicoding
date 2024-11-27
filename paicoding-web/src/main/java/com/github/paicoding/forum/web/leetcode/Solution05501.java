package com.github.paicoding.forum.web.leetcode;

public class Solution05501 {
    public boolean canJump(int[] nums) {
        int maxPos = 0;
        for(int i = 0;i < nums.length;i++){
            if(i > maxPos){//观察当前位置是否能跳
                return false;
            }
            maxPos = Math.max(maxPos, i + nums[i]);//更新能跳的最远位置
        }
        return true;
    }

    public static void main(String[] args) {
        Solution05501 solution = new Solution05501();
        int[] nums = {2,3,1,1,4};
        System.out.println(solution.canJump(nums)); // 输出: true
    }
}
