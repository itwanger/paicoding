package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/22/23
 */
public class SynchronizedExample {
    public void syncBlockMethod() {
        synchronized(this) {
            // 同步块体
        }
    }
}
