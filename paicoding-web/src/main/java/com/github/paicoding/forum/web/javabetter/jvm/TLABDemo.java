package com.github.paicoding.forum.web.javabetter.jvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class TLABDemo {
    public static void main(String[] args) {
        for (int i = 0; i < 10_000_000; i++) {
            allocate(); // 创建大量对象
        }
        System.gc(); // 强制触发垃圾回收
    }

    private static void allocate() {
        // 小对象分配，通常会使用 TLAB
        byte[] bytes = new byte[64];
    }
}
