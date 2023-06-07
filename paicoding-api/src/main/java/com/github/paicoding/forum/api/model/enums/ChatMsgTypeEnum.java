package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 6/7/23
 */
@Getter
public enum ChatMsgTypeEnum {
    // code desc
    // 连接成功
    Established(0, "Established"),
    // payload 消息
    Payload(1, "Payload"),
    // Closed 关闭
    Closed(2, "Closed"),
    ;

    private Integer type;
    private String msg;

    ChatMsgTypeEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static ChatMsgTypeEnum typeOf(int type) {
        for (ChatMsgTypeEnum value : ChatMsgTypeEnum.values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static ChatMsgTypeEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }


}
