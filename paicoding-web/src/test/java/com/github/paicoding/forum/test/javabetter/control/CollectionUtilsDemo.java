package com.github.paicoding.forum.test.javabetter.control;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtilsDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(1);
        list.add(3);

        if (CollectionUtils.isEmpty(list)) {
            System.out.println("集合为空");
        }

        if (CollectionUtils.isNotEmpty(list)) {
            System.out.println("集合不为空");
        }
    }
}
