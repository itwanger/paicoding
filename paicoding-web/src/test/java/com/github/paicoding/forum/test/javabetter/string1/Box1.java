package com.github.paicoding.forum.test.javabetter.string1;

public class Box1 {
    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        Long sum = 0L;
        for (int i = 0; i < Integer.MAX_VALUE;i++) {
            sum += i;
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
    }
}
