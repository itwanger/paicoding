package com.github.paicoding.forum.test.javabetter.jvm;

public class HeapSize {
    public static void main(String[] args) {
        long heapSize = Runtime.getRuntime().maxMemory();
        // 换算成 M 单位
        heapSize = heapSize / 1024 / 1024;
        System.out.println("Maximum Heap Size (M): " + heapSize);
    }
}
