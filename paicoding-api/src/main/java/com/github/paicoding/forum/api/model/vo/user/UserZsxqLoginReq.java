package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 知识星球登录
 *
 * @author YiHui
 * @date 2025/08/19
 */
@Data
@Accessors(chain = true)
public class UserZsxqLoginReq implements Serializable {
    private static final long serialVersionUID = 2139742660700910738L;
    /**
     * 知识星球用户id
     */
    private Long starUserId;
    /**
     * 登录用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String displayName;
    /**
     * 星球编号
     */
    private String starNumber;
    /**
     * 头像
     */
    private String avatar;

    /**
     * 过期时间(ms)
     */
    private Long expireTime;

    private Boolean updateUserInfo;
}
