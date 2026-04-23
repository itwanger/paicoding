package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 活跃登录会话
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_active_session")
public class UserActiveSessionDO extends BaseDO {
    private Long userId;
    private String loginName;
    private Integer loginType;
    private String sessionHash;
    private String deviceId;
    private String deviceName;
    private String uaHash;
    private String userAgent;
    private String ip;
    private String region;
    private Date latestSeenTime;
    private Date expireTime;
    private Date offlineTime;
    private String offlineReason;
}
