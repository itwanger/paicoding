package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 配置类型枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum ConfigTagEnum {

    EMPTY(0, ""),
    HOT(1, "热门"),
    OFFICAL(2, "官方"),
    COMMENT(3, "推荐"),
    ;

    ConfigTagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static ConfigTagEnum formCode(Integer code) {
        for (ConfigTagEnum value : ConfigTagEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ConfigTagEnum.EMPTY;
    }
}
