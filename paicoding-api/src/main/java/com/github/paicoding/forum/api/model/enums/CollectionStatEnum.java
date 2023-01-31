package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 收藏状态枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum CollectionStatEnum {

    NOT_COLLECTION(0, "未收藏"),
    COLLECTION(1, "已收藏"),
    CANCEL_COLLECTION(2, "取消收藏");

    CollectionStatEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static CollectionStatEnum formCode(Integer code) {
        for (CollectionStatEnum value : CollectionStatEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return CollectionStatEnum.NOT_COLLECTION;
    }
}
