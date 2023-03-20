package com.github.paicoding.forum.test.javabetter.io1;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathDemo {
    public static void main(String[] args) {
        // 使用绝对路径创建
        Path absolutePath = Paths.get("/Users/username/test/1.txt");
// 使用相对路径创建
        Path relativePath = Paths.get("test", "1.txt");
        System.out.println(absolutePath.equals(relativePath)); // true
    }
}
