package com.github.paicoding.forum.test.javabetter.io1;

import java.io.*;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/22/23
 */
public class LuanmaDemo {
    public static void main(String[] args) {
        String s = "沉默王二！";

        try {
            // 将字符串按UTF-8编码方式保存到文件中
            OutputStreamWriter outUtf8 = new OutputStreamWriter(
                    new FileOutputStream("logs/test_utf8.txt"), "UTF-8");
            outUtf8.write(s);
            outUtf8.close();

            // 将文件按GBK编码方式读取，并显示内容
            InputStreamReader inGbk = new InputStreamReader(
                    new FileInputStream("logs/test_utf8.txt"), "GBK");
            char[] buffer = new char[1024];
            int len;
            while ((len = inGbk.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, len));
            }
            inGbk.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
