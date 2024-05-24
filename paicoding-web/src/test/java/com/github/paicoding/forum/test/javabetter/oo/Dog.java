package com.github.paicoding.forum.test.javabetter.oo;

abstract class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public abstract void makeSound();
}

public class Dog extends Animal {
    private int age;

    public Dog(String name, int age) {
        super(name);  // 调用抽象类的构造函数
        this.age = age;
    }

    @Override
    public void makeSound() {
        System.out.println(name + " says: Bark");
    }
}
