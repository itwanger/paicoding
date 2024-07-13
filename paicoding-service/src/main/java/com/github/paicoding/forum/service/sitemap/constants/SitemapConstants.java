package com.github.paicoding.forum.service.sitemap.constants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 站点相关地图
 * @author XuYifei
 * @date 2024-07-12
 */
public class SitemapConstants {
    public static final String SITE_VISIT_KEY = "visit_info";

    public static String day(LocalDate day) {
        return DateTimeFormatter.ofPattern("yyyyMMdd").format(day);
    }
}
