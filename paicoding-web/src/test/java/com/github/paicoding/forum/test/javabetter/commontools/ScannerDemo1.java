package com.github.paicoding.forum.test.javabetter.commontools;

import java.util.Scanner;

public class ScannerDemo1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // 创建 Scanner 对象，从标准输入流中读取数据
        System.out.print("请输入一个整数：");
        if (scanner.hasNextInt()) { // 判断输入流中是否有下一个整数
            int num = scanner.nextInt(); // 读取输入流中的下一个整数
            System.out.println("您输入的整数是：" + num);
        } else {
            System.out.println("输入的不是整数！");
        }
        scanner.nextLine(); // 读取输入流中的换行符

        System.out.print("请输入多个单词，以空格分隔：");
        while (scanner.hasNext()) { // 判断输入流中是否还有下一个标记
            String word = scanner.next(); // 读取输入流中的下一个单词
            System.out.println("您输入的单词是：" + word);
        }
        scanner.nextLine(); // 读取输入流中的换行符

        System.out.print("请输入一个实数：");
        if (scanner.hasNextDouble()) { // 判断输入流中是否有下一个实数
            double num = scanner.nextDouble(); // 读取输入流中的下一个实数
            System.out.println("您输入的实数是：" + num);
        } else {
            System.out.println("输入的不是实数！");
        }
        scanner.nextLine(); // 读取输入流中的换行符

        System.out.print("请输入一个字符串：");
        if (scanner.hasNextLine()) { // 判断输入流中是否有下一行
            String line = scanner.nextLine(); // 读取输入流中的下一行
            System.out.println("您输入的字符串是：" + line);
        } else {
            System.out.println("输入的不是字符串！");
        }
        scanner.close(); // 关闭 Scanner 对象
    }
}
