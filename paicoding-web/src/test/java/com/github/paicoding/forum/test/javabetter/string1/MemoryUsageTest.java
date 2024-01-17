package com.github.paicoding.forum.test.javabetter.string1;

public class MemoryUsageTest {

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore, memoryAfter;
        int size = 1000000;

        // 测试基本类型 int 的内存占用
        runtime.gc();
        memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        int[] intArray = new int[size];
        memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("基本数据类型数组占用内存: " + (memoryAfter - memoryBefore));

        // 测试包装类型 Integer 的内存占用
        runtime.gc();
        memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        Integer[] integerArray = new Integer[size];
        for (int i = 0; i < size; i++) {
            integerArray[i] = i; // 自动装箱
        }
        memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("包装类型数组占用内存空间: " + (memoryAfter - memoryBefore));
    }
}
