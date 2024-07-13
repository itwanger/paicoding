package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public enum RoleEnum {
    NORMAL(0, "普通用户"),
    ADMIN(1, "超级用户"),
    ;

    @Getter
    private int role;
    @Getter
    private String desc;

    RoleEnum(int role, String desc) {
        this.role = role;
        this.desc = desc;
    }

    public static String role(Integer roleId) {
        if (Objects.equals(roleId, 1)) {
            return ADMIN.name();
        } else {
            return NORMAL.name();
        }
    }
}
