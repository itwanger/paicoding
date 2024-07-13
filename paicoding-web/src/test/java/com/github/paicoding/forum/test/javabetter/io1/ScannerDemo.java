package com.github.paicoding.forum.test.javabetter.io1;

import java.util.Scanner;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/26/23
 */
public class ScannerDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入一个字符串：");
        String str = scanner.nextLine();
        System.out.println("输入的字符串是：" + str);
        System.out.print("请输入一个整数：");
        int num = scanner.nextInt();
        System.out.println("输入的整数是：" + num);
    }
}
