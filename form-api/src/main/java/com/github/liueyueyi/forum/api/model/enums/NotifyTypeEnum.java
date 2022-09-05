package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Getter
public enum NotifyTypeEnum {
    COMMENT(1, "评论"),
    REPLY(2, "回复"),
    PRAISE(3, "点赞"),
    COLLECT(4, "收藏"),
    FOLLOW(5, "关注消息"),
    SYSTEM(6, "系统消息"),
    DELETE_COMMENT(11, "删除评论"),
    DELETE_REPLY(12, "删除回复"),
    CANCEL_PRAISE(13, "取消点赞"),
    CANCEL_COLLECT(14, "取消收藏"),
    CANCEL_FOLLOW(15, "取消关注"),
    ;


    private int type;
    private String msg;

    private static Map<Integer, NotifyTypeEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (NotifyTypeEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    NotifyTypeEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static NotifyTypeEnum typeOf(int type) {
        return mapper.get(type);
    }

    public static NotifyTypeEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }
}
