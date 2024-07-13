package com.github.paicoding.forum.test.javabetter.jvm;

import java.lang.ref.SoftReference;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/12/24
 */
public class SoftReferenceExample1 {
    public static void main(String[] args) {
        SoftReference<byte[]> softReference = new SoftReference<>(new byte[1024 * 1024 * 42]); // 分配约50MB内存

        System.out.println("GC 前: " + softReference.get());
        System.gc(); // 建议执行垃圾回收
        try {
            Thread.sleep(1000); // 稍微等待垃圾回收器运行
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("GC 后: " + softReference.get());
    }
}
