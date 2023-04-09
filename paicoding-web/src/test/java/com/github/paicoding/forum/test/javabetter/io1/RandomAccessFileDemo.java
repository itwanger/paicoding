package com.github.paicoding.forum.test.javabetter.io1;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileDemo {

    public static void main(String[] args) {
        String filePath = "logs/javabetter/itwanger.txt";

        try {
            // 使用 RandomAccessFile 写入文件
            writeToFile(filePath, "Hello, 沉默王二!");

            // 使用 RandomAccessFile 读取文件
            String content = readFromFile(filePath);
            System.out.println("文件内容: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw")) {
            // 将文件指针移动到文件末尾（在此处追加内容）
            randomAccessFile.seek(randomAccessFile.length());

            // 写入内容
            randomAccessFile.writeUTF(content);
        }
    }

    private static String readFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
            // 将文件指针移动到文件开始处（从头开始读取）
            randomAccessFile.seek(0);

            content.append(randomAccessFile.readUTF());
        }

        return content.toString();
    }
}

