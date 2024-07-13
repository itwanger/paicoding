package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 文档类型枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum DocumentTypeEnum {

    EMPTY(0, ""),
    ARTICLE(1, "文章"),
    COMMENT(2, "评论");

    DocumentTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static DocumentTypeEnum formCode(Integer code) {
        for (DocumentTypeEnum value : DocumentTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return DocumentTypeEnum.EMPTY;
    }
}
