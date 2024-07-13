package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterDemo {
    public static void main(String[] args) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter("output.txt");
            fw.write(72); // 写入字符'H'的ASCII码
            fw.write(101); // 写入字符'e'的ASCII码
            fw.write(108); // 写入字符'l'的ASCII码
            fw.write(108); // 写入字符'l'的ASCII码
            fw.write(111); // 写入字符'o'的ASCII码
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
