package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/12/24
 */
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class PhantomReferenceExample {
    public static void main(String[] args) {
        // 创建一个强引用的对象
        String strongReference = new String("二哥，我是强引用");

        // 创建一个引用队列
        ReferenceQueue<String> referenceQueue = new ReferenceQueue<>();

        // 创建一个虚引用，指向上面的对象，并与引用队列关联
        PhantomReference<String> phantomReference = new PhantomReference<>(strongReference, referenceQueue);

        // 取消强引用
        strongReference = null;

        // 强制进行垃圾回收
        System.gc();

        // 检查引用队列，看是否有通知
        if (referenceQueue.poll() != null) {
            System.out.println("虚引用的对象已被垃圾回收，且收到了通知");
        } else {
            System.out.println("虚引用的对象未被垃圾回收，或未收到通知");
        }
    }
}

