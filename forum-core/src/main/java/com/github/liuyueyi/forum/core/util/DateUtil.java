package com.github.liuyueyi.forum.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author YiHui
 * @date 2022/8/25
 */
public class DateUtil {

    /**
     * 毫秒转日期
     *
     * @param timestamp
     * @return
     */
    public static String time2day(long timestamp) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
    }
}
