package com.github.paicoding.forum.test.javabetter.commontools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ScannerFileDemo {
    public static void main(String[] args) {
        try {
            // 创建 File 对象，表示要扫描的文件
            File file = new File("docs/安装环境.md");
            Scanner scanner = new Scanner(file); // 创建 Scanner 对象，从文件中读取数据
            while (scanner.hasNextLine()) { // 判断文件中是否有下一行
                String line = scanner.nextLine(); // 读取文件中的下一行
                System.out.println(line); // 打印读取的行
            }
            scanner.close(); // 关闭 Scanner 对象
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在！");
        }
    }
}
