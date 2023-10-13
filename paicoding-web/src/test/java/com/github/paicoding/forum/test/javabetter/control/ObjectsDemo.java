package com.github.paicoding.forum.test.javabetter.control;

import java.util.Objects;

public class ObjectsDemo {
    public static void main(String[] args) {
        Integer integer = new Integer(1);

        if (Objects.isNull(integer)) {
            System.out.println("对象为空");
        }

        if (Objects.nonNull(integer)) {
            System.out.println("对象不为空");
        }

        Integer integer1 = new Integer(128);

        Objects.requireNonNull(integer1);
        Objects.requireNonNull(integer1, "参数不能为空");
        Objects.requireNonNull(integer1, () -> "参数不能为空");

        String str = new String("沉默王二");
        System.out.println(Objects.hashCode(str));
    }
}
