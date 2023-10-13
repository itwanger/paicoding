package com.github.paicoding.forum.test.javabetter.collection1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/15/23
 */
public class ModulusVsRemainder {
    public static void main(String[] args) {
        int a = -7;
        int b = 3;

        int remainder = a % b;
        int modulus = Math.floorMod(a, b);

        System.out.println("For numbers: a = " + a + ", b = " + b);
        System.out.println("Remainder (using %): " + remainder);
        System.out.println("Modulus (using Math.floorMod): " + modulus);

        a = 7;
        b = -3;

        remainder = a % b;
        modulus = Math.floorMod(a, b);

        System.out.println("\nFor numbers: a = " + a + ", b = " + b);
        System.out.println("Remainder (using %): " + remainder);
        System.out.println("Modulus (using Math.floorMod): " + modulus);
    }
}
