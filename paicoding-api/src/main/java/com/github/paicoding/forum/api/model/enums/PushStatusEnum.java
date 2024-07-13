package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 发布状态枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum PushStatusEnum {

    OFFLINE(0, "未发布"),
    ONLINE(1,"已发布"),
    REVIEW(2, "审核");

    PushStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public static PushStatusEnum formCode(int code) {
        for (PushStatusEnum yesOrNoEnum : PushStatusEnum.values()) {
            if (yesOrNoEnum.getCode() == code) {
                return yesOrNoEnum;
            }
        }
        return PushStatusEnum.OFFLINE;
    }
}
