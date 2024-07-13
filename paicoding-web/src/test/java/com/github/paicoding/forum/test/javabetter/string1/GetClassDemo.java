package com.github.paicoding.forum.test.javabetter.string1;

import com.github.paicoding.forum.test.javabetter.io1.Person;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/13/24
 */
public class GetClassDemo {
    public static void main(String[] args) {
        Person1 p = new Person1();
        Class<? extends Person1> aClass = p.getClass();
        System.out.println(aClass.getName());
    }
}

class Person1 {
    private String name;
    private int age;

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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Person1) {
            Person1 p = (Person1) obj;
            return this.name.equals(p.getName()) && this.age == p.getAge();
        }
        return false;
    }
}
