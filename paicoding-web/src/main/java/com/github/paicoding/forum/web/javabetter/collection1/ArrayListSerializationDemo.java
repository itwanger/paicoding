package com.github.paicoding.forum.web.javabetter.collection1;

import java.io.*;
import java.util.ArrayList;

public class ArrayListSerializationDemo {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Python");
        list.add("C++");

        // 序列化
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("list.ser"))) {
            oos.writeObject(list);
            System.out.println("ArrayList 已序列化");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 反序列化
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("list.ser"))) {
            ArrayList<String> deserializedList = (ArrayList<String>) ois.readObject();
            System.out.println("反序列化后的 ArrayList：" + deserializedList);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
