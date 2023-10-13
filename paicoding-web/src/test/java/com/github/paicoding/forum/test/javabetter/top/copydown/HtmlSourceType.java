package com.github.paicoding.forum.test.javabetter.top.copydown;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/28/22
 */
public enum HtmlSourceType {
    WEIXIN("weixin", "微信公众号"),
    JUEJIN("juejin", "掘金社区"),
    BOKEYUAN("cnblog", "博客园"),
    CSDN("csdn", "CSDN"),
    ZHIHU("zhihu", "知乎"),
    ITMIND("itmind", "小白学堂"),
    segmentfault("segmentfault", "思否"),
    newcoder("newcoder", "牛客"),
    github("github", "Github"),
    leetcode("leetcode", "LeetCode"),
    OTHER("other", "其他网站");

    private String name;
    private String category;

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    HtmlSourceType(String name, String category) {
        this.name = name;
        this.category = category;
    }

}
