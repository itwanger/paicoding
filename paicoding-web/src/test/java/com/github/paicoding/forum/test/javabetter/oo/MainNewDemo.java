package com.github.paicoding.forum.test.javabetter.oo;

class Parent {
    // 父类静态代码块
    static {
        System.out.println("父类静态代码块");
    }

    // 父类实例初始化块
    {
        System.out.println("父类实例初始化块");
    }

    // 父类构造方法
    public Parent() {
        System.out.println("父类构造方法");
    }
}

class Child extends Parent {
    // 子类静态代码块
    static {
        System.out.println("子类静态代码块");
    }

    // 子类实例初始化块
    {
        System.out.println("子类实例初始化块");
    }

    // 子类构造方法
    public Child() {
        System.out.println("子类构造方法");
    }
}

public class MainNewDemo {
    public static void main(String[] args) {
        new Child();
    }
}