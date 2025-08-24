package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户名密码登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Data
@Accessors(chain = true)
public class UserPwdLoginReq implements Serializable {
    private static final long serialVersionUID = -5941617870303218990L;

    private Long userId;
    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 星球编号
     */
    private String starNumber;

    /**
     * 星球过期时间
     */
    private Long starExpireTime;

    /**
     * 登录类型
     */
    private Integer loginType;
    
    /**
     * 第三方账号ID
     */
    private String thirdAccountId;
}