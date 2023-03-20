package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterDemo2 {
    public static void main(String[] args) throws IOException {
        String str = "沉默王二真的帅啊！";
        FileWriter fw = new FileWriter("output.txt");
            fw.write(str); // 将字符串的前 5 个字符写入文件
    }
}
