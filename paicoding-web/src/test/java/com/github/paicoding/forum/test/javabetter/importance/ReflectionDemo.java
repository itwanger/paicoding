package com.github.paicoding.forum.test.javabetter.importance;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/8/23
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Person {
    private String name;
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    private void privateMethod() {
        System.out.println("私有方法");
    }
}

public class ReflectionDemo {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        // 获取 Person 类的 Class 对象
        Class<?> personClass = Class.forName("com.github.paicoding.forum.test.javabetter.importance.Person");

        // 获取并打印类名
        System.out.println("类名: " + personClass.getName());

        // 获取构造函数
        Constructor<?> constructor = personClass.getConstructor(String.class, int.class);

        // 使用构造函数创建 Person 对象实例
        Object personInstance = constructor.newInstance("沉默王二", 30);

        // 获取并调用 getName 方法
        Method getNameMethod = personClass.getMethod("getName");
        String name = (String) getNameMethod.invoke(personInstance);
        System.out.println("名字: " + name);

        // 获取并调用 setAge 方法
        Method setAgeMethod = personClass.getMethod("setAge", int.class);
        setAgeMethod.invoke(personInstance, 35);

        // 获取并访问 age 字段
        Field ageField = personClass.getDeclaredField("age");
        ageField.setAccessible(true);
        int age = ageField.getInt(personInstance);
        System.out.println("年纪: " + age);

        // 获取并调用私有方法
        Method privateMethod = personClass.getDeclaredMethod("privateMethod");
        privateMethod.setAccessible(true);
        privateMethod.invoke(personInstance);
    }
}
