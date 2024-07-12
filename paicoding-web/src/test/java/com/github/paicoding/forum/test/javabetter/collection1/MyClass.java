package com.github.paicoding.forum.test.javabetter.collection1;

class MyClass implements InterfaceC {
    public void methodA() {
        System.out.println("Method A");
    }

    public void methodB() {
        System.out.println("Method B");
    }

    public void methodC() {
        System.out.println("Method C");
    }

    public static void main(String[] args) {
        MyClass myClass = new MyClass();
        myClass.methodA();
        myClass.methodB();
        myClass.methodC();
    }
}