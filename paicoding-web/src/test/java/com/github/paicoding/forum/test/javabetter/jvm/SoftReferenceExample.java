package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/12/24
 */
import java.lang.ref.SoftReference;

public class SoftReferenceExample {
    public static void main(String[] args) {
        // 创建一个强引用的对象
        String strongReference = new String("二哥，我是个强引用");

        // 创建一个软引用，指向上面的对象
        SoftReference<String> softReference = new SoftReference<>(strongReference);

        // 干掉强引用
        strongReference = null;

        // 现在只有软引用指向 "二哥，我是个强引用" 对象

        // 尝试通过软引用获取对象
        String retrievedString = softReference.get();
        System.out.println(retrievedString); // 输出 "二哥，我是个强引用"

        // 强制进行垃圾回收，可能会清除软引用的对象
        System.gc();

        // 再次尝试通过软引用获取对象
        retrievedString = softReference.get();
        if (retrievedString != null) {
            System.out.println(retrievedString);
        } else {
            System.out.println("软引用的对象已被垃圾回收");
        }
    }
}
