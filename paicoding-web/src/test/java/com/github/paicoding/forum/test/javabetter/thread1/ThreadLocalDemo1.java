package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/22/23
 */
public class ThreadLocalDemo1 {
    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "Initial Value");

        Thread thread = new Thread(() -> {
            System.out.println(threadLocal.get()); // 输出 "Initial Value"
            threadLocal.set("Updated Value");
            System.out.println(threadLocal.get()); // 输出 "Updated Value"
            threadLocal.remove();
            System.out.println(threadLocal.get()); // 输出 "Initial Value"
        });
        thread.start();
    }
}
