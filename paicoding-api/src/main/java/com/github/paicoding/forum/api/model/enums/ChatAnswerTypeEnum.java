package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

@Getter
public enum ChatAnswerTypeEnum {
    // 纯文本
    TEXT(0, "TEXT"),
    // JSON,
    JSON(1, "JSON"),
    /**
     * 流式返回
     */
    STREAM(2, "STREAM"),
    /**
     * 流式结束
     */
    STREAM_END(3, "STREAM_END")
    ;

    private Integer code;
    private String desc;


    ChatAnswerTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ChatAnswerTypeEnum typeOf(int type) {
        for (ChatAnswerTypeEnum value : ChatAnswerTypeEnum.values()) {
            if (value.code.equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static ChatAnswerTypeEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }
}
