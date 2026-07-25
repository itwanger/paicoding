package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 小程序账号密码登录请求。
 *
 * @author YiHui
 * @date 2026/7/25
 */
@Data
@Accessors(chain = true)
public class WxMiniPwdLoginReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名（或星球编号）
     */
    private String username;

    /**
     * 登录密码（明文，后端按 security.salt 配置加密后比对）
     */
    private String password;
}
