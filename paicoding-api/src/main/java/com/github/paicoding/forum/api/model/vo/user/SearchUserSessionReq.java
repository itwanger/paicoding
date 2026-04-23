package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 登录会话分页查询
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
public class SearchUserSessionReq {
    private Long userId;
    private String loginName;
    private String deviceId;
    private String ip;
    /**
     * true 仅查询当前有效会话
     */
    private Boolean activeOnly;
    private Long pageNumber;
    private Long pageSize;
}
