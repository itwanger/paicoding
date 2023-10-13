package com.github.paicoding.forum.test.javabetter.commontools;

import java.util.Scanner;

public class SannerDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // 创建 Scanner 对象，从标准输入流中读取数据
        System.out.print("请输入一个整数：");
        int num = scanner.nextInt(); // 获取用户输入的整数
        System.out.println("您输入的整数是：" + num);
        scanner.nextLine(); // 读取换行符，避免影响下一次读取
        System.out.print("请输入一个字符串：");
        String str = scanner.nextLine(); // 获取用户输入的字符串
        System.out.println("您输入的字符串是：" + str);
        scanner.close(); // 关闭 Scanner 对象
    }
}
