package com.github.paicoding.forum.core.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author YiHui
 * @date 2022/8/25
 */
public class DateUtil {
    public static final DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter BLOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    public static final DateTimeFormatter BLOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 毫秒转日期
     *
     * @param timestamp
     * @return
     */
    public static String time2day(long timestamp) {
        return format(BLOG_TIME_FORMAT, timestamp);
    }

    public static String time2day(Timestamp timestamp) {
        return time2day(timestamp.getTime());
    }

    public static LocalDateTime time2LocalTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public static String time2utc(long timestamp) {
        return format(UTC_FORMAT, timestamp);
    }

    public static String time2date(long timestamp) {
        return format(BLOG_DATE_FORMAT, timestamp);
    }

    public static String time2date(Timestamp timestamp) {
        return time2date(timestamp.getTime());
    }


    public static String format(DateTimeFormatter format, long timestamp) {
        LocalDateTime time = time2LocalTime(timestamp);
        return format.format(time);
    }
}
