package com.github.paicoding.forum.test.javabetter.collection1;

public class QumoQuyu {
    public static void main(String[] args) {
        int a = -10 % 3; // a = -1
        int b = Math.floorMod(-10, 3); // b = 2
        System.out.println(a);
        System.out.println(b);
    }
}
