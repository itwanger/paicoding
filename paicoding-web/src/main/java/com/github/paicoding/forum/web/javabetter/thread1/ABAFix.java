package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.atomic.AtomicStampedReference;

class ABAFix {
    private static AtomicStampedReference<String> ref = new AtomicStampedReference<>("100", 1);

    public static void main(String[] args) {
        new Thread(() -> {
            int stamp = ref.getStamp();
            ref.compareAndSet("100", "200", stamp, stamp + 1);
            ref.compareAndSet("200", "100", ref.getStamp(), ref.getStamp() + 1);
        }).start();

        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            int stamp = ref.getStamp();
            System.out.println("CAS 结果：" + ref.compareAndSet("100", "300", stamp, stamp + 1));
        }).start();
    }
}
