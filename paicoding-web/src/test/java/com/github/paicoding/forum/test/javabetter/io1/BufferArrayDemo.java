package com.github.paicoding.forum.test.javabetter.io1;

import java.io.*;

public class BufferArrayDemo {
    public static void main(String[] args) throws IOException {
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 创建流对象
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("py.mp4"));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("copyPy.mp4"));){
            // 读写数据
            int len;
            byte[] bytes = new byte[8*1024];
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0 , len);
            }
        }
        // 记录结束时间
        long end = System.currentTimeMillis();
        System.out.println("缓冲流使用数组复制时间:"+(end - start)+" 毫秒");
    }
}
