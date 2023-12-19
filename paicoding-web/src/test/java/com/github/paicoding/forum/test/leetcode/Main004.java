package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/13/23
 */
import java.util.Scanner;

public class Main004 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入第一个数组");
        // 读取一串字符，按照, 分割成 int 型数组
        String[] nums1Str = scanner.nextLine().split(",");
        int[] nums1 = new int[nums1Str.length];
        for (int i = 0; i < nums1Str.length; i++) {
            nums1[i] = Integer.parseInt(nums1Str[i]);
        }

        System.out.println("输入第二个数组");
        // 读取一串字符，按照, 分割成 int 型数组
        String[] nums2Str = scanner.nextLine().split(",");
        int[] nums2 = new int[nums2Str.length];
        for (int i = 0; i < nums2Str.length; i++) {
            nums2[i] = Integer.parseInt(nums2Str[i]);
        }

        Solution004 solution = new Solution004();
        double median = solution.findMedianSortedArrays(nums1, nums2);
        System.out.println("中位数是: " + median);
    }
}

class Solution004 {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int ans1 = getKth(nums1, 0, nums1.length - 1, nums2, 0, nums2.length - 1, (nums1.length + nums2.length + 1) / 2);
        int ans2 = getKth(nums1,0, nums1.length - 1, nums2, 0, nums2.length - 1, (nums1.length + nums2.length + 2) / 2);
        //如果是奇数，则两次求的k都是一样的，即(nums1.length + nums2.length + 1) / 2 == (nums1.length + nums2.length + 2) / 2
        return (ans1 + ans2) / 2.0;
    }

    private int getKth(int[] nums1, int start1, int end1, int[] nums2, int start2, int end2, int k) {
        int siz1 = end1 - start1 + 1;
        int siz2 = end2 - start2 + 1;
        if (siz1 > siz2) {
            return getKth(nums2, start2, end2, nums1, start1, end1, k);
        }
        //为了方便知道哪个数组首先为空，我们设置siz1严格小于等于siz2，这样子一旦有数组被清空，则一定是nums1
        if (siz1 == 0) {
            return nums2[start2 + k - 1];//已经把第一个数组全部排除，只需要求第二数组的剩下数字中的第 k 个就行了（更新后的k）
        }
        if (k == 1) {
            return Math.min(nums1[start1], nums2[start2]);//求当前两个数组中最小的数字，当然直接返回两个当前中最小的即可
        }
        int pos1 = start1 + Math.min(siz1, k / 2) - 1;
        int pos2 = start2 + Math.min(siz2, k / 2) - 1;

        if (nums1[pos1] > nums2[pos2]) {
            //排除第二个数组不符合条件的数字
            return getKth(nums1, start1, end1, nums2, pos2 + 1, end2, k - (pos2 - start2 + 1));
        }
        else {
            //排除第一个数组不符合条件的数字
            return getKth(nums1, pos1 + 1, end1, nums2, start2, end2, k - (pos1 - start1 + 1));
        }

    }
}

