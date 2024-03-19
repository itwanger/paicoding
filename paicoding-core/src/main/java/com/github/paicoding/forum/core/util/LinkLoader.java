package com.github.paicoding.forum.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * markdown文本中的图片识别
 *
 * @author YiHui
 * @date 2022/11/24
 */
public class LinkLoader {
    private static Pattern LINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        /**
         * 原始文本
         */
        private String origin;
        /**
         * 超链描述
         */
        private String desc;
        /**
         * 地址
         */
        private String url;
    }

    public static List<Link> loadLinks(String content) {
        Matcher matcher = LINK_PATTERN.matcher(content);
        List<Link> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(new Link(matcher.group(0), matcher.group(1), matcher.group(2)));
        }
        return list;
    }
}
