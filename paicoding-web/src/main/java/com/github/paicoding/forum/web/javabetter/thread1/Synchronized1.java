package com.github.paicoding.forum.web.javabetter.thread1;

class Synchronized1 {
    public static void main(String[] args) {
        // 假设我们有一个共享资源 x 和 flag
        int x;
        boolean flag = false;
        Object lock = new Object(); // 用于同步的锁对象

        synchronized(lock) {
            x = 1;
            flag = true;
        }

    }
}
