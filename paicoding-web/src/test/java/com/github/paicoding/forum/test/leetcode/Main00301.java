package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/12/23
 */
import java.util.HashMap;
import java.util.Scanner;

public class Main00301 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入字符串: ");
        String s = scanner.nextLine(); // 从标准输入读取字符串

        int length = lengthOfLongestSubstring(s); // 调用解题函数
        System.out.println("没有重复的最长子串为: " + length); // 输出结果
    }

    public static int lengthOfLongestSubstring(String s) {
        HashMap<Character, Integer> rempos = new HashMap<>();
        int res = 0;
        for (int i = 0, j = 0; i < s.length(); i++) {
            System.out.println("i: " + i + ", j: " + j);
            if (rempos.containsKey(s.charAt(i))) {
                j = Math.max(j, rempos.get(s.charAt(i)));
            }
            System.out.println("i: " + i + ", j: " + j);
            // 把子串也打印出来
            System.out.println("sub: " + s.substring(j, i + 1));
            res = Math.max(res, i - j + 1);
            rempos.put(s.charAt(i), i + 1);
        }
        return res;
    }
}

