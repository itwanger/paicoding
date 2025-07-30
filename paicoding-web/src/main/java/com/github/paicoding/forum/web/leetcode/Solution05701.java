package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.List;

public class Solution05701 {
    public int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int i = 0, n = intervals.length;

        // 1. 处理所有在 newInterval 之前的区间
        while (i < n && intervals[i][1] < newInterval[0]) {
            result.add(intervals[i]);
            i++;
        }

        // 2. 处理所有与 newInterval 重叠的区间
        while (i < n && intervals[i][0] <= newInterval[1]) {
            newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
            newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
            i++;
        }
        result.add(newInterval); // 插入合并后的区间

        // 3. 处理所有在 newInterval 之后的区间
        while (i < n) {
            result.add(intervals[i]);
            i++;
        }

        // 4. 返回结果
        return result.toArray(new int[result.size()][]);
    }

    public static void main(String[] args) {
        Solution05701 solution = new Solution05701();
        int[][] intervals = {{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}};
        int[] newInterval = {4, 8};
        System.out.println(java.util.Arrays.deepToString(solution.insert(intervals, newInterval)));
        // 输出: [[1,2], [3,10], [12,16]]
    }
}