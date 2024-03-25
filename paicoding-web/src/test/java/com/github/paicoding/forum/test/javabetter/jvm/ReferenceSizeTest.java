package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/20/24
 */
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

public class ReferenceSizeTest {
    public static void main(String[] args) {
        Object obj = new Object();
        Object[] array = new Object[1];

        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseInstance(obj).toPrintable());
        System.out.println(ClassLayout.parseInstance(array).toPrintable());
    }
}
