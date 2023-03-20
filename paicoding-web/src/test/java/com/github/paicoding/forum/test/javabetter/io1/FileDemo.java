package com.github.paicoding.forum.test.javabetter.io1;

import java.io.File;

public class FileDemo {
    public static void main(String[] args) {
        File directory = new File("/Users/itwanger/Documents/Github/paicoding");

        // 列出目录下的文件名
        String[] files = directory.list();
        System.out.println("目录下的文件名：");
        for (String file : files) {
            System.out.println(file);
        }

        // 列出目录下的文件和子目录
        File[] filesAndDirs = directory.listFiles();
        System.out.println("目录下的文件和子目录：");
        for (File fileOrDir : filesAndDirs) {
            if (fileOrDir.isFile()) {
                System.out.println("文件：" + fileOrDir.getName());
            } else if (fileOrDir.isDirectory()) {
                System.out.println("目录：" + fileOrDir.getName());
            }
        }
    }
}
