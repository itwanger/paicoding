package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/7/24
 */
public class Main02501 {
    public static void main(String[] args) {
        ListNode head = new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5)))));
        Solution02501 solution = new Solution02501();
        ListNode result = solution.reverseKGroup(head, 4);
        System.out.println(result);
    }

}

class Solution02501 {
    public ListNode reverseKGroup(ListNode head, int k) {
        // 初始化 tail 用于遍历找到第 k 个节点
        ListNode tail = head;
        for (int i = 1; i <= k; i++) {
            // 如果不足 k 个，则不进行翻转，直接返回原链表头部
            if (tail == null) return head;
            tail = tail.next;
        }
        // 翻转当前 k 个元素的链表，并返回新的头节点
        ListNode newHead = reverse(head, tail);
        // 递归处理剩下的链表，并将当前翻转的链表的尾部（原 head）连接到翻转后的剩余链表上
        head.next = reverseKGroup(tail, k);
        return newHead;
    }

    private ListNode reverse(ListNode head, ListNode tail) {
        ListNode prev = null;
        ListNode next;
        while (head != tail) {
            // 保存当前节点的下一个节点
            next = head.next;
            // 将当前节点的 next 指向 prev，完成翻转
            head.next = prev;
            // prev 和 head 前进一位
            prev = head;
            head = next;
        }
        // 返回翻转后的链表的头节点
        return prev;
    }
}
