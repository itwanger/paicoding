package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/22/23
 */
public class ExceptionExample {
    public void testException() {
        try {
            int a = 1 / 0; // 这将导致除以零的异常
        } catch (ArithmeticException e) {
            System.out.println("发生算术异常");
        }
    }
}