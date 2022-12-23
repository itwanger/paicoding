package com.github.liuyueyi.forum.core.util;


import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YiHui
 * @date 2022/12/23
 */
public class ArticleUtil {
    private static final Integer MAX_SUMMARY_CHECK_TXT_LEN = 2000;
    private static final Integer SUMMARY_LEN = 128;
    private static Pattern LINK_IMG_PATTERN = Pattern.compile("!?\\[(.*?)\\]\\((.*?)\\)");
    private static Pattern CONTENT_PATTERN = Pattern.compile("[0-9a-zA-Z\u4e00-\u9fa5:;\"'<>,.?/·~！：；“”‘’《》，。？、（）]");

    public static String pickSummary(String summary) {
        if (StringUtils.isBlank(summary)) {
            return StringUtils.EMPTY;
        }

        // 首先移除所有的图片，链接
        summary = summary.substring(0, Math.min(summary.length(), MAX_SUMMARY_CHECK_TXT_LEN)).trim();
        summary = summary.replaceAll(LINK_IMG_PATTERN.pattern(), "");

        // 取第一个汉字索引的位置
        int beginIndex = 0;
        for (int index = 0, len = summary.length(); index < len; index++) {
            String word = summary.substring(index, index + 1);
            if (word.compareTo("\u4e00") > 0 && word.compareTo("\u9fa5") < 0) {
                beginIndex = index;
                break;
            }
        }
        summary = summary.substring(beginIndex);

        // 匹配对应字符
        StringBuilder result = new StringBuilder();
        Matcher matcher = CONTENT_PATTERN.matcher(summary);
        while (matcher.find()) {
            result.append(summary, matcher.start(), matcher.end());
            if (summary.length() >= SUMMARY_LEN) {
                return summary.trim();
            }
        }
        return summary.trim();
    }
}
