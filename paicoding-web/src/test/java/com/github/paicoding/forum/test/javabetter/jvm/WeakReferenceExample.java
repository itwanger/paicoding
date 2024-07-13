package com.github.paicoding.forum.test.javabetter.jvm;

import java.lang.ref.WeakReference;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/12/24
 */
public class WeakReferenceExample {
    public static void main(String[] args) {
        // 创建一个强引用的对象
        String strongReference = new String("二哥，我是强引用");

        // 创建一个弱引用，指向上面的对象
        WeakReference<String> weakReference = new WeakReference<>(strongReference);

        // 取消强引用
        strongReference = null;

        // 强制进行垃圾回收
        System.gc();

        // 尝试通过弱引用获取对象
        String retrievedString = weakReference.get();
        if (retrievedString != null) {
            System.out.println(retrievedString);
        } else {
            System.out.println("弱引用的对象已被垃圾回收");
        }
    }
}
