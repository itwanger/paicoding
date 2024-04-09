package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/16/24
 */
public class ThreadLocalExample {
    // 创建一个ThreadLocal变量来存储每个线程特有的用户ID
    private static final ThreadLocal<Integer> userId = ThreadLocal.withInitial(() -> null);

    public static void main(String[] args) throws InterruptedException {
        // 模拟两个用户访问，分别在两个线程中处理
        Thread user1 = new Thread(() -> {
            // 为当前线程的userId变量设置值
            userId.set(1);
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + " userId: " + userId.get());
            // 模拟一些处理工作
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 使用完毕后清理ThreadLocal变量，防止内存泄露
            userId.remove();
        }, "User1-Thread");

        Thread user2 = new Thread(() -> {
            // 为当前线程的userId变量设置值
            userId.set(2);
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + " userId: " + userId.get());
            // 模拟一些处理工作
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 使用完毕后清理ThreadLocal变量，防止内存泄露
            userId.remove();
        }, "User2-Thread");

        // 启动线程
        user1.start();
        user2.start();

        // 等待线程完成
        user1.join();
        user2.join();
    }
}
