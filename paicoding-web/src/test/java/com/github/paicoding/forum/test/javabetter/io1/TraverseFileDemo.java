package com.github.paicoding.forum.test.javabetter.io1;

import java.io.File;

public class TraverseFileDemo {
    public static void main(String[] args) {
        File directory = new File("/Users/itwanger/Documents/Github/paicoding");

        // 递归遍历目录下的文件和子目录
        traverseDirectory(directory);
    }

    public static void traverseDirectory(File directory) {
        // 列出目录下的所有文件和子目录
        File[] filesAndDirs = directory.listFiles();

        // 遍历每个文件和子目录
        for (File fileOrDir : filesAndDirs) {
            if (fileOrDir.isFile()) {
                // 如果是文件，输出文件名
                System.out.println("文件：" + fileOrDir.getName());
            } else if (fileOrDir.isDirectory()) {
                // 如果是目录，递归遍历子目录
                System.out.println("目录：" + fileOrDir.getName());
                traverseDirectory(fileOrDir);
            }
        }
    }
}
