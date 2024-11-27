package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 文章阅读类型枚举
 *
 * @author YiHui
 * @date 2024/10/29
 */
@Getter
public enum ArticleReadTypeEnum {
    NORMAL(0, "直接阅读"),
    LOGIN(1, "登录阅读"),
    TIME_READ(2, "限时阅读"),
    STAR_READ(3, "星球阅读"),
    PAY_READ(4, "付费阅读"),
    ;

    private Integer type;

    private String desc;

    ArticleReadTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static ArticleReadTypeEnum typeOf(Integer type) {
        for (ArticleReadTypeEnum t : values()) {
            if (Objects.equals(type, t.type)) {
                return t;
            }
        }
        return null;
    }
}
