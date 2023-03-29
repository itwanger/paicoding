package com.github.paicoding.forum.test.javabetter.control;

import java.util.Objects;

public class ObjectsDemo1 {
    public static void main(String[] args) {
        Person person1 = new Person("沉默王二", 18);
        Person person2 = new Person("沉默王二", 18);

        System.out.println(Objects.equals(person1, person2)); // 输出：false
    }
}
class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}


