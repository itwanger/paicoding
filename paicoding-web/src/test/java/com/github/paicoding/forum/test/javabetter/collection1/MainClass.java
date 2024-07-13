package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/12/24
 */
public class MainClass {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Integer> list = new ArrayList<>();

        while (sc.hasNext()) {
            int number = sc.nextInt();
            if (number == -1) {
                break;
            }
            list.add(number);

        }
        System.out.println(list);
    }
}
