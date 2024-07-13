package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/18/23
 */
import java.util.concurrent.LinkedBlockingDeque;

public class LinkedBlockingDequeDemo {
    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingDeque<String> deque = new LinkedBlockingDeque<>(10);

        // Adding elements at the end of the deque
        deque.putLast("Item1");
        deque.putLast("Item2");

        // Adding elements at the beginning of the deque
        deque.putFirst("Item3");

        // Removing elements from the beginning
        System.out.println(deque.takeFirst()); // Output: Item3

        // Removing elements from the end
        System.out.println(deque.takeLast()); // Output: Item2
    }
}

