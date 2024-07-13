package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户入参
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class UserSaveReq {
    /**
     * 主键ID
     */
    private Long userId;

    /**
     * 第三方用户ID
     */
    private String thirdAccountId;

    /**
     * 登录方式: 0-微信登录，1-账号密码登录
     */
    private Integer loginType;
}
