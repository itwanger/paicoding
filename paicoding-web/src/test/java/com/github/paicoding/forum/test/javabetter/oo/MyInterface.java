package com.github.paicoding.forum.test.javabetter.oo;

public interface MyInterface {
    default void myDefaultMethod() {
        System.out.println("My default method");
    }

    static void myStaticMethod() {
        System.out.println("My static method");
    }
}