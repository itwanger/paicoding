package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;

import java.util.Date;

/**
 * 疑似共享账号视图
 *
 * @author Codex
 * @date 2026/4/25
 */
@Data
public class UserShareRiskDTO {
    private Long userId;
    private String loginName;
    private String userNickname;
    private String starNumber;
    private Long kickoutCount;
    private Long loginSuccessCount;
    private Long deviceCount;
    private Long ipCount;
    private Date lastKickoutTime;
    private Date lastActiveTime;
    private String riskLevel;
    private String riskReason;
    private Boolean forbidden;
    private Date forbidUntil;
    private String forbidReason;
}
