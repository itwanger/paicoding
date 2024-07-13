package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 用户页面选择枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum HomeSelectEnum {

    ARTICLE("article", "文章"),
    READ("read", "浏览记录"),
    FOLLOW("follow", "关注"),
    COLLECTION("collection", "收藏");

    HomeSelectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    public static HomeSelectEnum fromCode(String code) {
        for (HomeSelectEnum value : HomeSelectEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return HomeSelectEnum.ARTICLE;
    }
}
