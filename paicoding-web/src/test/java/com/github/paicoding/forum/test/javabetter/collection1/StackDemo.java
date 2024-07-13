package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.Stack;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/12/23
 */
public class StackDemo {
    public static void main(String[] args) {
        // stack
        Stack<String> stack = new Stack<>();

        // 增加元素
        stack.push("A");
        stack.push("B");
        stack.push("C");

        // 获取栈顶元素
        String top = stack.peek();
        System.out.println("栈顶元素为：" + top); // C

        // 弹出栈顶元素
        String pop = stack.pop();
        System.out.println("弹出的元素为：" + pop); // C

        // 修改栈顶元素
        stack.set(1, "D");
        System.out.println("修改后的栈为：" + stack); // [A, D]

        // 查找元素
        int index = stack.search("A");
        if (index == -1) {
            System.out.println("元素不存在");
        } else {
            System.out.println("元素在栈中的位置为：" + index); // 2
        }
    }
}
