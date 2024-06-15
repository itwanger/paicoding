package com.github.paicoding.forum.test.javabetter.integer1;

import org.openjdk.jol.info.ClassLayout;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 6/2/24
 */
public class SingleBooleanExample {
    boolean flag;

    public static void main(String[] args) {
        SingleBooleanExample example = new SingleBooleanExample();
        System.out.println(ClassLayout.parseInstance(example).toPrintable());
    }
}
