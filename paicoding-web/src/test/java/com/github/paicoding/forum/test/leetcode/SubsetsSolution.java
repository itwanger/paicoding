package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/14/23
 */
import java.util.ArrayList;
import java.util.List;

public class SubsetsSolution {

    public static void main(String[] args) {
        // 从命令行参数中解析输入
        int[] nums = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            nums[i] = Integer.parseInt(args[i]);
        }

        SubsetsSolution solution = new SubsetsSolution();
        List<List<Integer>> result = solution.subsets(nums);

        // 打印结果
        for (List<Integer> subset : result) {
            System.out.println(subset);
        }
    }

    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrack(nums, new ArrayList<>(), 0, res);
        return res;
    }

    public void backtrack(int[] nums, List<Integer> current, int start, List<List<Integer>> res) {
        res.add(new ArrayList<>(current));
        for (int i = start; i < nums.length; i++) {
            current.add(nums[i]);
            backtrack(nums, current, i + 1, res);
            current.remove(current.size() - 1);
        }
    }
}
