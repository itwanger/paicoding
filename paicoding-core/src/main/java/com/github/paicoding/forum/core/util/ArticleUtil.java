package com.github.paicoding.forum.core.util;


import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class ArticleUtil {
    private static final Integer MAX_SUMMARY_CHECK_TXT_LEN = 2000;
    private static final Integer SUMMARY_LEN = 256;
    private static Pattern LINK_IMG_PATTERN = Pattern.compile("!?\\[(.*?)\\]\\((.*?)\\)");
    private static Pattern CONTENT_PATTERN = Pattern.compile("[0-9a-zA-Z\u4e00-\u9fa5:;\"'<>,.?/·~！：；“”‘’《》，。？、（）]");

    private static Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    public static String pickSummary(String summary) {
        if (StringUtils.isBlank(summary)) {
            return StringUtils.EMPTY;
        }

        // 首先移除所有的图片，链接
        summary = summary.substring(0, Math.min(summary.length(), MAX_SUMMARY_CHECK_TXT_LEN)).trim();
        // 移除md的图片、超链
        summary = summary.replaceAll(LINK_IMG_PATTERN.pattern(), "");
        // 移除html标签
        summary = HTML_TAG_PATTERN.matcher(summary).replaceAll("");

        // 匹配对应字符
        StringBuilder result = new StringBuilder();
        Matcher matcher = CONTENT_PATTERN.matcher(summary);
        while (matcher.find()) {
            result.append(summary, matcher.start(), matcher.end());
            if (result.length() >= SUMMARY_LEN) {
                return result.substring(0, SUMMARY_LEN).trim();
            }
        }
        return result.toString().trim();
    }
}
