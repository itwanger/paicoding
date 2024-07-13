package com.github.paicoding.forum.test.javabetter.collection1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/23/24
 */
public class HashMapDemo1 {
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public static void main(String[] args) {
        int n = 16;
        System.out.println(hash(5) & (n-1));
        System.out.println(hash(21) & (n-1));

        n = 32;
        System.out.println(hash(5) & (n-1));
        System.out.println(hash(21) & (n-1));
    }
}
