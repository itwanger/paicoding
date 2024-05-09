package com.github.paicoding.forum.test.leetcode;

public class Main02601 {
    public static void main(String[] args) {
        Solutions02601 solutions = new Solutions02601();
        int[] nums = {0,0,1,1,1,2,2,3,3,4};
        int result = solutions.removeDuplicates(nums);
        System.out.println(result);
    }
}

class Solutions02601 {
    public int removeDuplicates(int[] nums) {
        int i = 0,j = 0;
        for(;i < nums.length;i++) {
            if (nums[i] != nums[j]) {
                nums[++j] = nums[i];
            }
        }
        return j + 1;
    }
}