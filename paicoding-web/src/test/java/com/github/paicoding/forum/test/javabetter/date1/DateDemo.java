package com.github.paicoding.forum.test.javabetter.date1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateDemo {
    public static void main(String[] args) {
        LocalDate today = LocalDate.now();
        System.out.println("Today's Local date : " + today);

        LocalTime time = LocalTime.now();
        System.out.println("Local time : " + time);

        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current DateTime : " + now);
    }
}
