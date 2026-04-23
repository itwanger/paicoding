package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录审计日志
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_login_audit")
public class UserLoginAuditDO extends BaseDO {
    private Long userId;
    private String loginName;
    private Integer loginType;
    private String eventType;
    private String deviceId;
    private String deviceName;
    private String uaHash;
    private String userAgent;
    private String ip;
    private String region;
    private String sessionHash;
    private String riskTag;
    private String reason;
}
