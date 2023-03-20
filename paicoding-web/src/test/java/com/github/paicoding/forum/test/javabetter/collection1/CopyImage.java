package com.github.paicoding.forum.test.javabetter.collection1;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/18/23
 */
public class CopyImage {
    public static void main(String[] args) throws Exception {
        // 创建一个 FileInputStream 对象以读取原始图片文件
        FileInputStream fis = new FileInputStream("original.jpg");

        // 创建一个 FileOutputStream 对象以写入复制后的图片文件
        FileOutputStream fos = new FileOutputStream("copy.jpg");

        // 创建一个缓冲区数组以存储读取的数据
        byte[] buffer = new byte[1024];
        int count;

        // 读取原始图片文件并将数据写入复制后的图片文件
        while ((count = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, count);
        }

        // 关闭输入流和输出流
        fis.close();
        fos.close();
    }
}
