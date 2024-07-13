package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 关注类型枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum FollowTypeEnum {

    FOLLOW("follow", "我关注的用户"),
    FANS("fans", "关注我的粉丝");

    FollowTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    public static FollowTypeEnum formCode(String code) {
        for (FollowTypeEnum value : FollowTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return FollowTypeEnum.FOLLOW;
    }
}
