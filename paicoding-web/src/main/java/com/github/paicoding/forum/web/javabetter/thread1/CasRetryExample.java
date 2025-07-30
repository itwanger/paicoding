package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.atomic.AtomicInteger;

class CasRetryExample {
    private static AtomicInteger counter = new AtomicInteger(0);
    private static final int MAX_RETRIES = 5;

    public static void main(String[] args) {
        boolean success = false;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            int currentValue = counter.get();
            boolean updated = counter.compareAndSet(currentValue, currentValue + 1);

            if (updated) {
                System.out.println("更新成功，当前值: " + counter.get());
                success = true;
                break;
            } else {
                retries++;
                System.out.println("更新失败，进行第 " + retries + " 次重试");
            }
        }

        if (!success) {
            System.out.println("达到最大重试次数，操作失败");
        }
    }
}