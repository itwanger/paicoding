package com.github.paicoding.forum.test.javabetter.collection1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/18/23
 */
public class FileOutputStreamDemo {
    public static void main(String[] args) throws FileNotFoundException {
//        String fileName = "example.txt";
//        FileOutputStream fos = new FileOutputStream(fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("example.txt");
//            fos.write("沉默王二".getBytes());

            fos.write(120);
            fos.write('x');
            fos.write(0x12345678);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
