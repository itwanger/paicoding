package com.github.paicoding.forum.web.leetcode;

public class Main04501 {
    public static void main(String[] args) {
        Main04501 solution = new Main04501();
        System.out.println(solution.jump(new int[]{2, 3, 1, 1, 4})); // 输出 2
        System.out.println(solution.jump(new int[]{2, 3, 0, 1, 4})); // 输出 2
    }

    public int jump(int[] nums) {
        int n = nums.length;
        int end = 0;
        int maxPosition = 0;
        int steps = 0;
        for (int i = 0; i < n - 1; i++) {
            maxPosition = Math.max(maxPosition, i + nums[i]);
            if (i == end) {
                end = maxPosition;
                steps++;
            }
        }
        return steps;
    }
}
