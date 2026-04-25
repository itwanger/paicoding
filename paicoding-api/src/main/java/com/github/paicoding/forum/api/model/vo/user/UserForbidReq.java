package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 用户禁用请求
 *
 * @author Codex
 * @date 2026/4/25
 */
@Data
public class UserForbidReq {
    private Long userId;
    private Integer days;
    private String reason;
}
