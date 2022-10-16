package com.github.liuyueyi.forum.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author LouZai
 * @date 2022/9/7
 */
public class LocalDateTimeUtil {

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String getCurrentDateTime() {
        return getCurrentDateTime(DEFAULT_FORMATTER);
    }

    public static String getCurrentDateTime(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

}