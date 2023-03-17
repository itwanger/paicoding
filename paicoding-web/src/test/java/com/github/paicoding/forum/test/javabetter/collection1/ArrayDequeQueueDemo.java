package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/13/23
 */
public class ArrayDequeQueueDemo {
    public static void main(String[] args) {
        ArrayDeque<String> queue = new ArrayDeque<>();

        // 增加元素
        queue.offer("沉默");
        queue.offer("王二");
        queue.offer("陈清扬");

        // 获取队首元素
        String front = queue.peek();
        System.out.println("队首元素为：" + front); // 沉默

        // 弹出队首元素
        String poll = queue.poll();
        System.out.println("弹出的元素为：" + poll); // 沉默

        // 修改队列中的元素
        queue.poll();
        queue.offer("小明");
        System.out.println("修改后的队列为：" + queue); // [陈清扬, 小明]

        // 查找元素
        Iterator<String> iterator = queue.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.equals("王二")) {
                System.out.println("元素在队列中的位置为：" + index); // 0
                break;
            }
            index++;
        }
    }
}
