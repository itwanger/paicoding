package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/24/23
 */
public class StackOverflowErrorTest {
    public static void main(String[] args) {
        StackOverflowErrorTest test = new StackOverflowErrorTest();
        test.testStackOverflowError();
    }

    public void testStackOverflowError() {
        testStackOverflowError();
    }
}
