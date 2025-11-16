package com.github.paicoding.forum.test.mapstrcut;

import java.util.Date;

public class MapperTime {
    public static void main(String[] args) {
        Date date = new Date(1695288951000L); // 获取当前时间
        long timestamp = date.getTime(); // 获取时间戳

        System.out.println("当前时间的时间戳：" + timestamp);
        System.out.println(date);

        Date date1 = new Date(1699929998573L); // 获取当前时间
        long timestamp1 = date1.getTime(); // 获取时间戳

        System.out.println("当前时间的时间戳：" + timestamp1);
        System.out.println(date1);
    }
}
