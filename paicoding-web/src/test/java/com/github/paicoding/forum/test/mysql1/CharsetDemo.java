package com.github.paicoding.forum.test.mysql1;

public class CharsetDemo {
    public static void main(String[] args) {
        // 请输出 空格的 ASCII 码，以及对应的二进制
        char c = ' ';
        System.out.println((int) c);
        System.out.println(Integer.toBinaryString(c));

        // 请输出 A 的 ASCII 码，以及对应的二进制
        c = 'A';
        System.out.println((int) c);
        System.out.println(Integer.toBinaryString(c));
    }
}
