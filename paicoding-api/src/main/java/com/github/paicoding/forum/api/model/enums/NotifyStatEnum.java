package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Getter
public enum NotifyStatEnum {
    UNREAD(0, "未读"),
    READ(1, "已读");


    private int stat;
    private String msg;

    NotifyStatEnum(int type, String msg) {
        this.stat = type;
        this.msg = msg;
    }
}
