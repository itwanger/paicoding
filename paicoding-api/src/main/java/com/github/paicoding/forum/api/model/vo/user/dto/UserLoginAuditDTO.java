package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;

import java.util.Date;

/**
 * 后台登录审计视图
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
public class UserLoginAuditDTO {
    private Long id;
    private Long userId;
    private String loginName;
    private String userNickname;
    private String starNumber;
    private Integer loginType;
    private String loginTypeDesc;
    private String eventType;
    private String eventTypeDesc;
    private String deviceId;
    private String deviceName;
    private String ip;
    private String region;
    private String sessionHash;
    private String riskTag;
    private String reason;
    private Date createTime;
}
