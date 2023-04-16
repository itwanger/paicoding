package com.github.paicoding.forum.test.javabetter.io1;

import java.io.*;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/22/23
 */
public class InputStreamReaderDemo {
    public static void main(String[] args) {
        String s = "沉默王二！";

        try {
            // 将字符串按UTF-8编码方式保存到文件中
            OutputStreamWriter outUtf8 = new OutputStreamWriter(
                    new FileOutputStream("logs/test_utf8.txt"), "GBK");
            outUtf8.write(s);
            outUtf8.close();

            // 将字节流转换为字符流，使用UTF-8编码方式
            InputStreamReader isr = new InputStreamReader(new FileInputStream("logs/test_utf8.txt"), "GBK");
            // 读取字符流
            int c;
            while ((c = isr.read()) != -1) {
                System.out.print((char) c);
            }
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
