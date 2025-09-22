package com.github.paicoding.forum.web.javabetter.thread1;

/**
 * volatile关键字单线程性能测试
 * 测试volatile和非volatile变量在单线程环境下的性能差异
 * 
 * @author 沉默王二
 */
public class VolatilePerformanceTest {
    
    // 普通变量
    private static int normalVar = 0;
    
    // volatile变量
    private static volatile int volatileVar = 0;
    
    // 测试次数
    private static final int TEST_COUNT = 100_000_000;
    
    public static void main(String[] args) {
        System.out.println("开始单线程volatile性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println();
        
        // 测试普通变量读写性能
        testNormalVariable();
        
        // 测试volatile变量读写性能
        testVolatileVariable();
        
        // 对比测试
        comparePerformance();
    }
    
    /**
     * 测试普通变量的读写性能
     */
    private static void testNormalVariable() {
        System.out.println("=== 测试普通变量性能 ===");
        
        // 测试写入性能
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            normalVar = i;
        }
        long writeTime = System.nanoTime() - startTime;
        
        // 测试读取性能
        startTime = System.nanoTime();
        int temp;
        for (int i = 0; i < TEST_COUNT; i++) {
            temp = normalVar;
        }
        long readTime = System.nanoTime() - startTime;
        
        System.out.println("普通变量写入耗时: " + writeTime / 1_000_000 + " ms");
        System.out.println("普通变量读取耗时: " + readTime / 1_000_000 + " ms");
        System.out.println("普通变量总耗时: " + (writeTime + readTime) / 1_000_000 + " ms");
        System.out.println();
    }
    
    /**
     * 测试volatile变量的读写性能
     */
    private static void testVolatileVariable() {
        System.out.println("=== 测试volatile变量性能 ===");
        
        // 测试写入性能
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            volatileVar = i;
        }
        long writeTime = System.nanoTime() - startTime;
        
        // 测试读取性能
        startTime = System.nanoTime();
        int temp;
        for (int i = 0; i < TEST_COUNT; i++) {
            temp = volatileVar;
        }
        long readTime = System.nanoTime() - startTime;
        
        System.out.println("volatile变量写入耗时: " + writeTime / 1_000_000 + " ms");
        System.out.println("volatile变量读取耗时: " + readTime / 1_000_000 + " ms");
        System.out.println("volatile变量总耗时: " + (writeTime + readTime) / 1_000_000 + " ms");
        System.out.println();
    }
    
    /**
     * 对比测试两种变量的性能差异
     */
    private static void comparePerformance() {
        System.out.println("=== 性能对比测试 ===");
        
        // 多次测试取平均值
        final int testRounds = 10;
        long normalTotalTime = 0;
        long volatileTotalTime = 0;
        
        for (int round = 0; round < testRounds; round++) {
            // 测试普通变量
            long startTime = System.nanoTime();
            for (int i = 0; i < TEST_COUNT; i++) {
                normalVar = i;
                int temp = normalVar;
            }
            normalTotalTime += (System.nanoTime() - startTime);
            
            // 测试volatile变量
            startTime = System.nanoTime();
            for (int i = 0; i < TEST_COUNT; i++) {
                volatileVar = i;
                int temp = volatileVar;
            }
            volatileTotalTime += (System.nanoTime() - startTime);
        }
        
        long normalAvg = normalTotalTime / testRounds / 1_000_000;
        long volatileAvg = volatileTotalTime / testRounds / 1_000_000;
        
        System.out.println("经过 " + testRounds + " 轮测试的平均结果:");
        System.out.println("普通变量平均耗时: " + normalAvg + " ms");
        System.out.println("volatile变量平均耗时: " + volatileAvg + " ms");
        
        if (volatileAvg > normalAvg) {
            double overhead = ((double) (volatileAvg - normalAvg) / normalAvg) * 100;
            System.out.println("volatile性能开销: " + String.format("%.2f", overhead) + "%");
        } else {
            System.out.println("在此测试中，volatile变量表现更好或相当");
        }
        
        System.out.println();
        System.out.println("=== 结论 ===");
        System.out.println("1. volatile主要影响编译器优化和内存可见性");
        System.out.println("2. 单线程下性能差异通常很小（几个百分点）");
        System.out.println("3. volatile的主要作用是保证多线程间的可见性");
        System.out.println("4. 不应该仅为性能考虑而避免使用volatile");
    }
}