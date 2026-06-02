package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author YiHui
 * @date 2023/1/31
 */
public enum RoleEnum {
    NORMAL(0, "普通用户"),
    ADMIN(1, "超级用户"),
    OPERATOR(2, "运营"),
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
        for (RoleEnum role : values()) {
            if (Objects.equals(roleId, role.getRole())) {
                return role.name();
            }
        }
        return NORMAL.name();
    }
}
