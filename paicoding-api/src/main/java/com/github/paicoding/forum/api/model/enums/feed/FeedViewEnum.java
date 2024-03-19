package com.github.paicoding.forum.api.model.enums.feed;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 信息流的可视作用域
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Getter
@NoArgsConstructor
public enum FeedViewEnum {
    ALL(0, "所有人可见"),
    LOGIN(1, "登录可见"),
    FANS(2, "粉丝可见"),
    SELF(3, "自己可见"),
    ;

    private int value;
    private String desc;

    FeedViewEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FeedViewEnum valueOf(int value) {
        for (FeedViewEnum view : values()) {
            if (view.value == value) {
                return view;
            }
        }
        return null;
    }
}
