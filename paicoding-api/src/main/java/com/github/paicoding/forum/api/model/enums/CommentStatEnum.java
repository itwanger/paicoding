package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 评论状态枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum CommentStatEnum {

    NOT_COMMENT(0, "未评论"),
    COMMENT(1, "已评论"),
    DELETE_COMMENT(2, "删除评论");

    CommentStatEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static CommentStatEnum formCode(Integer code) {
        for (CommentStatEnum value : CommentStatEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return CommentStatEnum.NOT_COMMENT;
    }
}
