package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 运营账号创建入参
 *
 * @author Codex
 * @date 2026/5/30
 */
@Data
public class OperatorAccountCreateReq implements Serializable {
    private static final long serialVersionUID = -8048319667306765539L;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码；为空时后端自动生成
     */
    private String password;

    /**
     * 后台展示昵称
     */
    private String displayName;
}
