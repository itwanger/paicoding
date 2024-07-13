package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.Stack;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/1/24
 */
public class StackDemo1 {
    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();
        stack.push("沉默王二");
        stack.push("沉默王三");
        stack.push("一个文章真特么有趣的程序员");

        System.out.println(stack);
    }
}
