package com.github.paicoding.forum.test.javabetter.oo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/13/24
 */
public class FansheDemo {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        Class c2 = Class.forName("com.github.paicoding.forum.test.javabetter.oo.Writer");
//        Constructor constructor = c2.getConstructor();

        Constructor[] constructors1 = String.class.getDeclaredConstructors();
        for (Constructor c : constructors1) {
            System.out.println(c);
        }

        Field[] fields1 = System.class.getFields();
        Field fields2 = System.class.getField("out");

        System.out.println(fields2);
    }
}
class Writer {
    private int age;
    private String name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}