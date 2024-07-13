package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 关注用户枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum FollowSelectEnum {

    FOLLOW("follow", "关注列表"),
    FANS("fans", "粉丝列表");

    FollowSelectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    public static FollowSelectEnum fromCode(String code) {
        for (FollowSelectEnum value : FollowSelectEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return FollowSelectEnum.FOLLOW;
    }
}
