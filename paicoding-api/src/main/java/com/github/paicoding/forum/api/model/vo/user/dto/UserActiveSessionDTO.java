package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;

import java.util.Date;

/**
 * 后台登录会话视图
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
public class UserActiveSessionDTO {
    private Long id;
    private Long userId;
    private String loginName;
    private Integer loginType;
    private String loginTypeDesc;
    private String sessionHash;
    private String deviceId;
    private String deviceName;
    private String ip;
    private String region;
    private String sessionState;
    private String sessionStateDesc;
    private String offlineReason;
    private Date latestSeenTime;
    private Date expireTime;
    private Date offlineTime;
    private Date createTime;
}
