package com.github.paicoding.forum.test.javabetter.oo;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/13/24
 */
class Animal {}
class Dog extends Animal {}
class Cat extends Animal {}
class Puppy extends Dog {} // 假设 Dog 有一个子类 Puppy

public class FanxingMain {
    public static void main(String[] args) {
        List<? super Dog> animals = new ArrayList<>();
        animals.add(new Dog());    // 合法
        animals.add(new Puppy());  // 合法，因为 Puppy 是 Dog 的子类
        // animals.add(new Cat()); // 非法，编译错误，Cat 不是 Dog 或其子类

        // 读取时，只能安全地假定列表中的元素是 Object 类型
        for (Object obj : animals) {
            System.out.println(obj);
        }
    }
}
