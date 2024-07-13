package com.github.paicoding.forum.api.model.enums.column;

import lombok.Getter;

/**
 * 发布状态枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum ColumnTypeEnum {

    FREE(0, "免费"),
    LOGIN(1, "登录阅读"),
    TIME_FREE(2, "限时免费"),
    STAR_READ(3, "星球阅读"),
    ;

    ColumnTypeEnum(int code, String desc) {
        this.type = code;
        this.desc = desc;
    }

    private final int type;
    private final String desc;

    public static ColumnTypeEnum formCode(int code) {
        for (ColumnTypeEnum status : values()) {
            if (status.getType() == code) {
                return status;
            }
        }
        return ColumnTypeEnum.FREE;
    }
}
