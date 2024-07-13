package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章操作枚举
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Getter
public enum ArticleEventEnum {
    CREATE(1, "创建"),
    ONLINE(2, "发布"),
    REVIEW(3, "审核"),
    DELETE(4, "删除"),
    OFFLINE(5, "下线"),
    ;


    private int type;
    private String msg;

    private static Map<Integer, ArticleEventEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (ArticleEventEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    ArticleEventEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static ArticleEventEnum typeOf(int type) {
        return mapper.get(type);
    }

    public static ArticleEventEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }
}
