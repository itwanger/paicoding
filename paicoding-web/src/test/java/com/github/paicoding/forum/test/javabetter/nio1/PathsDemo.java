package com.github.paicoding.forum.test.javabetter.nio1;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsDemo {
    // 创建一个Path实例，表示当前目录下的一个文件
    Path path = Paths.get("example.txt");

    // 创建一个绝对路径
    Path absolutePath = Paths.get("/home/user/example.txt");

    public static void main(String[] args) {
        Path path = Paths.get("docs/配套教程.md");

        // 获取文件名
        System.out.println("File name: " + path.getFileName());

        // 获取父目录
        System.out.println("Parent: " + path.getParent());

        // 获取根目录
        System.out.println("Root: " + path.getRoot());

        // 将路径与另一个路径结合
        Path newPath = path.resolve("config/app.properties");
        System.out.println("Resolved path: " + newPath);

        // 简化路径
        Path normalizedPath = newPath.normalize();
        System.out.println("Normalized path: " + normalizedPath);

        // 将相对路径转换为绝对路径
        Path absolutePath = path.toAbsolutePath();
        System.out.println("Absolute path: " + absolutePath);

        // 计算两个路径之间的相对路径
        Path basePath = Paths.get("/docs/");
        Path targetPath = Paths.get("/docs/imgs/itwanger");
        Path relativePath = basePath.relativize(targetPath);
        System.out.println("Relative path: " + relativePath);
    }
}
