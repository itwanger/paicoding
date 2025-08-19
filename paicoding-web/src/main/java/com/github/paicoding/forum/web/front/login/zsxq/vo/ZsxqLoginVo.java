package com.github.paicoding.forum.web.front.login.zsxq.vo;

import lombok.Data;

/**
 * 知识星球的登录方式
 *
 * @author YiHui
 * @date 2025/8/19
 */
@Data
public class ZsxqLoginVo {
    /**
     * 应用id
     */
    private String app_id;

    /**
     * 过期时间(s)
     */
    private Long expire_time;

    /**
     * 扩展字段
     */
    private String extra;

    /**
     * 星球号
     */
    private String group_number;

    /**
     * 加入时间(s)
     */
    private Long join_time;

    /**
     * 签名
     */
    private String signature;

    /**
     * 时间戳(s)
     */
    private Long timestamp;

    /**
     * 用户头像
     */
    private String user_icon;

    /**
     * 用户id
     */
    private Long user_id;

    /**
     * 用户名
     */
    private String user_name;

    /**
     * 用户编号
     */
    private String user_number;

    /**
     * 用户角色
     */
    private String user_role;
}
