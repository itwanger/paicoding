package com.github.paicoding.forum.test.javabetter.oo;

abstract class Animal1 {
    protected String name;

    public Animal1(String name) {
        this.name = name;
    }

    public abstract void makeSound();
}

public class Dog1 extends Animal1 {
    private int age;

    public Dog1(String name, int age) {
        super(name);  // 调用抽象类的构造函数
        this.age = age;
    }

    @Override
    public void makeSound() {
        System.out.println(name + " says: Bark");
    }
}
