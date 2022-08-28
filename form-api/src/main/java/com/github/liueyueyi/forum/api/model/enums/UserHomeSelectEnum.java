package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * 用户页面选择枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum UserHomeSelectEnum {

    ARTICLE("article", "文章"),
    READ("read", "浏览记录"),
    FOLLOW("follow", "关注"),
    COLLECTION("collection", "收藏");

    UserHomeSelectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    public static UserHomeSelectEnum formCode(String code) {
        for (UserHomeSelectEnum value : UserHomeSelectEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return UserHomeSelectEnum.ARTICLE;
    }
}
