package com.github.paicoding.forum.test.leetcode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/31/24
 */
class Main01801 {
    public static void main(String[] args) {
        Solution01801 solution = new Solution01801();
        ListNode head = new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5)))));
        ListNode ans = solution.removeNthFromEnd(head, 4);
        System.out.println(ans);
    }
}

class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

class Solution01801 {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        // 创建一个虚拟头节点，简化边界条件处理
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // 第一次遍历，计算链表的总长度
        int length = 0;
        ListNode current = head;
        while (current != null) {
            length++;
            current = current.next;
        }

        // 设置长度为到达要删除的节点的前一个节点
        int index = length - n;
        current = dummy;
        // 第二次遍历，找到要删除的节点的前一个节点
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        // 删除节点，即跳过要删除的节点
        current.next = current.next.next;

        return dummy.next;
    }
}