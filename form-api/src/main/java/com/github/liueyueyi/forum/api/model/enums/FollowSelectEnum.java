package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * 关注用户枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum FollowSelectEnum {

    FOLLOW("follow", "我关注的用户"),
    FANS("fans", "关注我的粉丝");

    FollowSelectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    public static FollowSelectEnum formCode(String code) {
        for (FollowSelectEnum value : FollowSelectEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return FollowSelectEnum.FOLLOW;
    }
}
