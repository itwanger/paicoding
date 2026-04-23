package com.github.paicoding.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录审计事件
 *
 * @author Codex
 * @date 2026/4/23
 */
@Getter
@AllArgsConstructor
public enum LoginAuditEventEnum {
    LOGIN_SUCCESS("LOGIN_SUCCESS", "登录成功"),
    LOGIN_FAIL("LOGIN_FAIL", "登录失败"),
    LOGOUT("LOGOUT", "主动退出"),
    SESSION_KICKOUT("SESSION_KICKOUT", "会话被踢下线");

    private final String code;
    private final String desc;

    public static LoginAuditEventEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LoginAuditEventEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
