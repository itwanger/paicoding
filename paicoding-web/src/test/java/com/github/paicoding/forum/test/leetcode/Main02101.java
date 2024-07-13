package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/2/24
 */
public class Main02101 {
    public static void main(String[] args) {
        ListNode l1 = new ListNode(1, new ListNode(2, new ListNode(4)));
        ListNode l2 = new ListNode(1, new ListNode(3, new ListNode(4)));
        Solution02101 solution = new Solution02101();
        ListNode result = solution.mergeTwoLists(l1, l2);
        System.out.println(result);
    }
}


class Solution02101 {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        // 如果 l1 为空，返回 l2
        if (l1 == null) {
            return l2;
        }
        // 如果 l2 为空，返回 l1
        if (l2 == null) {
            return l1;
        }

        // 选择较小的节点，递归地合并剩余部分
        if (l1.val < l2.val) {
            l1.next = mergeTwoLists(l1.next, l2);
            return l1;
        } else {
            l2.next = mergeTwoLists(l1, l2.next);
            return l2;
        }
    }
}