package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * 操作文章
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum FlagBitEnum {

    OFFICAL(1, "官方"), // 1 > 0
    TOPPING(2, "置顶"), // 1 >> 1
    CREAM(4, "加精"); // 1 >> 2

    FlagBitEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static FlagBitEnum formCode(Integer code) {
        for (FlagBitEnum value : FlagBitEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return FlagBitEnum.OFFICAL;
    }
}
