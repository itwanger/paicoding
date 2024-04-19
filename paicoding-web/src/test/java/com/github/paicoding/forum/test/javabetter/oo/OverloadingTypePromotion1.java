package com.github.paicoding.forum.test.javabetter.oo;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/13/24
 */
public class OverloadingTypePromotion1 {
    void sum(int a, int b) {
        System.out.println("int");
    }

    void sum(long a, long b) {
        System.out.println("long");
    }

    public static void main(String args[]) {
        OverloadingTypePromotion1 obj = new OverloadingTypePromotion1();
        obj.sum(20, 20);
    }
}
