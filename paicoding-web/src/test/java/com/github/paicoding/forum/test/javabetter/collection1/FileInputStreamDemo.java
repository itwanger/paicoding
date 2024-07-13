package com.github.paicoding.forum.test.javabetter.collection1;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/18/23
 */
public class FileInputStreamDemo {
    public static void main(String[] args) throws IOException {
        // 创建一个 FileInputStream 对象
        FileInputStream fis = new FileInputStream("test.txt");

// 读取文件内容
        int data;
        while ((data = fis.read()) != -1) {
            System.out.print((char) data);
        }

// 关闭输入流
        fis.close();
    }
}
