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
     * 登录用户
     */
    LOGIN,
    /**
     * 所有用户
     */
    ALL;

    /**
     * 判断用户是否为超管
     *
     * @param role
     * @return
     */
    public static boolean adminUser(String role) {
        return ADMIN.name().equalsIgnoreCase(role);
    }
}
