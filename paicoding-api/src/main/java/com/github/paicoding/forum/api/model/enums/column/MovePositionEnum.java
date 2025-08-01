package com.github.paicoding.forum.api.model.enums.column;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2025/7/31
 */
@Getter
public enum MovePositionEnum {
    BEFORE(-1, "前"),
    AFTER(1, "后"),

    IN(0,"里");

    private Integer code;
    private String desc;

    MovePositionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
