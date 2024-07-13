package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 关注状态枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum FollowStateEnum {

    EMPTY(0, ""),
    FOLLOW(1, "关注"),
    CANCEL_FOLLOW(2, "取消关注");

    FollowStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static FollowStateEnum formCode(Integer code) {
        for (FollowStateEnum value : FollowStateEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return FollowStateEnum.EMPTY;
    }
}
