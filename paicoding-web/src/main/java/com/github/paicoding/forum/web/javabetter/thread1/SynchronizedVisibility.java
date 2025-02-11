package com.github.paicoding.forum.web.javabetter.thread1;

class SynchronizedVisibility {
    private static boolean flag = true;

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (SynchronizedVisibility.class) {
                while (flag) {} // 线程 A 现在一定能看到 flag=false
                System.out.println("线程 A 退出");
            }
        }).start();

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        synchronized (SynchronizedVisibility.class) {
            flag = false; // 线程 B 修改 flag
        }
    }
}