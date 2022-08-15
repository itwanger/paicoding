package com.github.liueyueyi.forum.api.model.vo.constants;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2022/7/27
 */
@Getter
public enum StatusEnum {
    SUCCESS(0, "OK"),

    ILLEGAL_ARGUMENTS(400_001, "参数异常"),
    ILLEGAL_ARGUMENTS_MIXED(400_002, "参数异常:%s"),

    LOGIN_FAILED_MIXED(403_001, "登录失败:%s"),
    ;

    private int code;

    private String msg;

    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
