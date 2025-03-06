package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.Exchanger;

public class ExchangerExample {
    private static final Exchanger<String> exchanger = new Exchanger<>();

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                String threadAData = "数据 A";
                System.out.println("线程 A 交换前的数据：" + threadAData);
                String received = exchanger.exchange(threadAData);
                System.out.println("线程 A 收到的数据：" + received);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                String threadBData = "数据 B";
                System.out.println("线程 B 交换前的数据：" + threadBData);
                String received = exchanger.exchange(threadBData);
                System.out.println("线程 B 收到的数据：" + received);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
