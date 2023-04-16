package com.github.paicoding.forum.test.javabetter.io1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileDemo1 {
    public static void main(String[] args) {
        File file = new File("logs/javabetter/itwanger.txt");

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // 写入文件
            raf.writeUTF("Hello, 沉默王二!");

            // 将文件指针移动到文件开头
            raf.seek(0);

            // 读取文件内容
            String content = raf.readUTF();
            System.out.println("内容: " + content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
