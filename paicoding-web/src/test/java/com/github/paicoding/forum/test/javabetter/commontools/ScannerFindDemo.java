package com.github.paicoding.forum.test.javabetter.commontools;

import java.util.Scanner;

public class ScannerFindDemo {
    public static void main(String[] args) {
        String input = "good good study, day day up.";
        Scanner scanner = new Scanner(input);
        String result;

        // 使用 findInLine() 方法查找字符串中的单词
        result = scanner.findInLine("study");
        System.out.println("findInLine(): " + result); // 输出 "study"

        // 使用 findWithinHorizon() 方法查找字符串中的单词
        scanner = new Scanner(input);
        result = scanner.findWithinHorizon("study", 20);
        System.out.println("findWithinHorizon(): " + result); // 输出 "study"

        scanner.close(); // 关闭 Scanner 对象
    }
}
