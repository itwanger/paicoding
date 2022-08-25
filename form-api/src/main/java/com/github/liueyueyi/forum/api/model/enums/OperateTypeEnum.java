package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * 操作类型
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum OperateTypeEnum {

    EMPTY(0, ""),
    READ(1, "阅读"),
    PRAISE(2, "点赞"),
    COLLECTION(3, "收藏"),
    CANCEL_PRAISE(4, "取消点赞"),
    CANCEL_COLLECTION(5, "取消收藏");

    OperateTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static OperateTypeEnum fromCode(Integer code) {
        for (OperateTypeEnum value : OperateTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return OperateTypeEnum.EMPTY;
    }
}
