package com.github.paicoding.forum.web.javabetter.thread1;

class InheritableThreadLocalExample {
    private static final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

    public static void main(String[] args) {
        inheritableThreadLocal.set("父线程的值");

        new Thread(() -> {
            System.out.println("子线程获取的值：" + inheritableThreadLocal.get()); // 继承了父线程的值
        }).start();
    }
}