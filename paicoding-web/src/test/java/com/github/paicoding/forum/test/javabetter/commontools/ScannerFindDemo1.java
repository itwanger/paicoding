package com.github.paicoding.forum.test.javabetter.commontools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ScannerFindDemo1 {
    public static void main(String[] args) throws FileNotFoundException {
        // 创建 File 对象，表示要扫描的文件
        Scanner scanner = new Scanner(new File("docs/安装环境.md")); // 创建 Scanner 对象，从文件中读取数据
        Pattern pattern = Pattern.compile("op..jdk");
        String result;
        while ((result = scanner.findWithinHorizon(pattern, 0)) != null) {
            System.out.println("findWithinHorizon(): " + result);
        }

    }
}
