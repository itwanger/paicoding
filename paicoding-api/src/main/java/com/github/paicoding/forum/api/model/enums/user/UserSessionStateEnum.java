package com.github.paicoding.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话状态
 *
 * @author Codex
 * @date 2026/4/23
 */
@Getter
@AllArgsConstructor
public enum UserSessionStateEnum {
    ACTIVE("ACTIVE", "在线"),
    OFFLINE("OFFLINE", "已下线"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String desc;
}
