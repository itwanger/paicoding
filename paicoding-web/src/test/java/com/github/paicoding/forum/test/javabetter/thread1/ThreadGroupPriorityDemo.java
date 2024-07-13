package com.github.paicoding.forum.test.javabetter.thread1;

public class ThreadGroupPriorityDemo {
    public static void main(String[] args) {
        // 创建一个线程组
        ThreadGroup group = new ThreadGroup("testGroup");
        // 将线程组的优先级指定为 7
        group.setMaxPriority(7);
        // 创建一个线程，将该线程加入到 group 中
        Thread thread = new Thread(group, "test-thread");
        // 企图将线程的优先级设定为 10
        thread.setPriority(10);
        // 输出线程组的优先级和线程的优先级
        System.out.println("线程组的优先级是：" + group.getMaxPriority());
        System.out.println("线程的优先级是：" + thread.getPriority());
    }
}
