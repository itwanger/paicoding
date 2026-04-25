package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 登录审计分页查询
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
public class SearchUserLoginAuditReq {
    private Long userId;
    private String loginName;
    private String starNumber;
    private String deviceId;
    private String ip;
    private String eventType;
    private Long pageNumber;
    private Long pageSize;
}
