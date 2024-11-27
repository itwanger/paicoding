package com.github.paicoding.forum.test.mysql1;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 2/2/24
 */
public class TimestampDemo {
    public static void main(String[] args) {
        // 现在的时间是2024-02-02 11:12:13，求对应的时间戳
        String dateTimeStr = "2024-02-02 11:12:13";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析字符串为 LocalDateTime 对象
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        // 转换为时间戳
        long timestamp = dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println("北京时间：" + dateTimeStr + " 对应的时间戳是：" + timestamp);

        // 把这个时间戳转换为纽约时间
        LocalDateTime newyorkDateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.ofHours(-5));
        System.out.println("纽约时间：" + newyorkDateTime.format(formatter));
    }
}
