package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 疑似共享账号状态
 *
 * @author Codex
 * @date 2026/5/12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_share_risk_account")
public class UserShareRiskAccountDO extends BaseDO {
    private Long userId;
    private String loginName;
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
    private Long forbidOperatorId;
    private Integer recentDays;
    private Date lastReleaseAt;
    private String lastHandleReason;
}
