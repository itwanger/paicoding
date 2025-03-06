package com.github.paicoding.forum.web.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Solution05601 {
    public int[][] merge(int[][] intervals) {
        if (intervals.length <= 1) {
            return intervals; // 如果区间少于等于1，直接返回
        }

        // 按区间的起始值排序
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        // 存储合并后的区间
        List<int[]> result = new ArrayList<>();

        // 初始化当前区间为第一个区间
        int[] currentInterval = intervals[0];

        // 遍历剩余区间
        for (int i = 1; i < intervals.length; i++) {
            // 如果当前区间和遍历的区间有重叠
            if (currentInterval[1] >= intervals[i][0]) {
                // 合并区间，更新当前区间的结束值
                currentInterval[1] = Math.max(currentInterval[1], intervals[i][1]);
            } else {
                // 没有重叠，将当前区间加入结果，并更新当前区间
                result.add(currentInterval);
                currentInterval = intervals[i];
            }
        }

        // 将最后一个区间加入结果
        result.add(currentInterval);

        // 转换结果为二维数组返回
        return result.toArray(new int[result.size()][]);
    }

    public static void main(String[] args) {
        Solution05601 solution = new Solution05601();
        int[][] intervals = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        System.out.println(Arrays.deepToString(solution.merge(intervals))); // 输出: [[1, 6], [8, 10], [15, 18]]
    }
}
