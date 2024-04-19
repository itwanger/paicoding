package com.github.paicoding.forum.test.javabetter.oo;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/15/24
 */
public class FinalDemo {
    public static void main(String[] args) {
        final StringBuilder sb = new StringBuilder("abc");
        sb.append("d");
        System.out.println(sb);  //abcd
    }
}
