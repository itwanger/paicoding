package com.github.paicoding.forum.test.javabetter.commontools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ScannerFileDemo1 {
    public static void main(String[] args) throws FileNotFoundException {
        // 创建 File 对象，表示要扫描的文件
        Scanner scanner = new Scanner(new File("docs/安装环境.md")); // 创建 Scanner 对象，从文件中读取数据
        scanner.useDelimiter("\\Z"); // 设置分隔符为文件结尾
        if (scanner.hasNext()) { // 判断文件中是否有下一行
            String content = scanner.next(); // 读取文件中的下一行
            System.out.println(content); // 打印读取的行
        }
        scanner.close(); // 关闭 Scanner 对象
    }
}
