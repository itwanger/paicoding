package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 回复状态枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum RecoverStatEnum {

    NOT_RECOVER(0, "未回复"),
    RECOVER(1, "已回复"),
    DELETE_RECOVER(2, "删除回复");

    RecoverStatEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static RecoverStatEnum formCode(Integer code) {
        for (RecoverStatEnum value : RecoverStatEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return RecoverStatEnum.NOT_RECOVER;
    }
}
