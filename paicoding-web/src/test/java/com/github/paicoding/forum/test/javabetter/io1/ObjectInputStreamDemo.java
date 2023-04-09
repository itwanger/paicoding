package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectInputStreamDemo {
    public static void main(String[] args) {
        String filename = "logs/person.dat"; // 待反序列化的文件名
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            // 从指定的文件输入流中读取对象并反序列化
            Object obj = in.readObject();
            // 将反序列化后的对象强制转换为指定类型
            Person p = (Person) obj;
            // 打印反序列化后的对象信息
            System.out.println("Deserialized Object: " + p);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
