package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class WalkFileTreeExample {
    public static void main(String[] args) {
        Path startingDir = Paths.get("docs");
        MyFileVisitor fileVisitor = new MyFileVisitor();

        try {
            Files.walkFileTree(startingDir, fileVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MyFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            System.out.println("准备访问目录: " + dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            System.out.println("正在访问目录: " + dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            System.out.println("访问文件: " + file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.err.println("访问文件失败: " + file);
            return FileVisitResult.CONTINUE;
        }
    }
}
