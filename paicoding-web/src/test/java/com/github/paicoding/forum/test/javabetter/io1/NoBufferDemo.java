package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NoBufferDemo {
    public static void main(String[] args) throws IOException {
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 创建流对象
        try (FileInputStream fis = new FileInputStream("py.exe");//exe文件够大
             FileOutputStream fos = new FileOutputStream("copyPy.exe")){
            // 读写数据
            int b;
            while ((b = fis.read()) != -1) {
                fos.write(b);
            }
        }
        // 记录结束时间
        long end = System.currentTimeMillis();
        System.out.println("普通流复制时间:"+(end - start)+" 毫秒");
    }
}
