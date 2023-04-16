package com.github.paicoding.forum.test.javabetter.io1;

import java.io.*;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/22/23
 */
public class HuanchongBufferDemo {
    public static void main(String[] args) {
        try {
            // 从文件读取字节流，使用UTF-8编码方式
            FileInputStream fis = new FileInputStream("test.txt");
            // 将字节流转换为字符流，使用UTF-8编码方式
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            // 使用缓冲流包装字符流，提高读取效率
            BufferedReader br = new BufferedReader(isr);
            // 创建输出流，使用UTF-8编码方式
            FileOutputStream fos = new FileOutputStream("output.txt");
            // 将输出流包装为转换流，使用UTF-8编码方式
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            // 使用缓冲流包装转换流，提高写入效率
            BufferedWriter bw = new BufferedWriter(osw);

            // 读取输入文件的每一行，写入到输出文件中
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine(); // 每行结束后写入一个换行符
            }

            // 关闭流
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
