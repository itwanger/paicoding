package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.PriorityQueue;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/13/23
 */
public class PriorityQueueDemo {
    public static void main(String[] args) {
        // 创建 PriorityQueue 对象
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();

        // 添加元素到 PriorityQueue
        priorityQueue.offer("沉默王二");
        priorityQueue.offer("陈清扬");
        priorityQueue.offer("小转铃");

        // 打印 PriorityQueue 中的元素
        System.out.println("PriorityQueue 中的元素：");
        while (!priorityQueue.isEmpty()) {
            System.out.print(priorityQueue.poll() + " ");
        }
    }
}
