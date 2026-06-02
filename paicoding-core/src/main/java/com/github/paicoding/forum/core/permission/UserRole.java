package com.github.paicoding.forum.core.permission;

/**
 * @author YiHui
 * @date 2022/8/25
 */
public enum UserRole {
    /**
     * 管理员
     */
    ADMIN,
    /**
     * 运营
     */
    OPERATOR,
    /**
     * 登录用户
     */
    LOGIN,
    /**
     * 所有用户
     */
    ALL;

    public boolean isAllowed(String role) {
        if (this == ALL || this == LOGIN) {
            return true;
        }
        if (this == ADMIN || this == OPERATOR) {
            return hasAdminPermission(role);
        }
        return false;
    }

    public static boolean hasAdminPermission(String role) {
        return ADMIN.name().equalsIgnoreCase(role) || OPERATOR.name().equalsIgnoreCase(role);
    }
}
