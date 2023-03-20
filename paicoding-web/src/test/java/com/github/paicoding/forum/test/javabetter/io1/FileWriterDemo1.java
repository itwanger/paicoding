package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterDemo1 {
    public static void main(String[] args) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("output.txt");
            char[] chars = {'H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!'};
            fw.write(chars, 0, 5); // 将字符数组的前 5 个字符写入文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
