package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Getter
public enum NotifyTypeEnum {
    COMMENT(1, "评论"),
    REPLY(2, "回复"),
    PRAISE(3, "点赞"),
    COLLECT(4, "收藏"),
    FOLLOW(5, "关注消息"),
    SYSTEM(6, "系统消息"),
    DELETE_COMMENT(1, "删除评论"),
    DELETE_REPLY(2, "删除回复"),
    CANCEL_PRAISE(3, "取消点赞"),
    CANCEL_COLLECT(4, "取消收藏"),
    CANCEL_FOLLOW(5, "取消关注"),

    // 注册、登录添加系统相关提示消息
    REGISTER(6, "用户注册"),
    BIND(6, "绑定星球"),
    LOGIN(6, "用户登录"),
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
