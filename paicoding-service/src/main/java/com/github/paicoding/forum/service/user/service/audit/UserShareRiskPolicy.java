package com.github.paicoding.forum.service.user.service.audit;

import org.springframework.stereotype.Component;

/**
 * 共享账号风险策略
 *
 * @author Codex
 * @date 2026/5/10
 */
@Component
public class UserShareRiskPolicy {
    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";
    public static final int AUTO_FORBID_DAYS = 7;
    public static final String AUTO_FORBID_REASON = "系统检测到高风险共享行为，自动禁用7天";
    public static final String AUTO_UNFORBID_REASON = "近7天已不再满足高风险共享规则，系统自动解除禁用";

    public String resolveRiskLevel(long kickoutCount, long deviceCount, long ipCount) {
        if (kickoutCount >= 5 && deviceCount >= 3 && ipCount >= 2) {
            return HIGH;
        }
        if (kickoutCount >= 3 && (deviceCount >= 2 || ipCount >= 2)) {
            return MEDIUM;
        }
        return LOW;
    }

    public boolean isHighRisk(long kickoutCount, long deviceCount, long ipCount) {
        return HIGH.equals(resolveRiskLevel(kickoutCount, deviceCount, ipCount));
    }

    public String buildRiskReason(int recentDays, long kickoutCount, long deviceCount, long ipCount) {
        String level = resolveRiskLevel(kickoutCount, deviceCount, ipCount);
        if (HIGH.equals(level)) {
            return "设备/IP频繁变化";
        }
        if (MEDIUM.equals(level)) {
            return "多次被踢下线";
        }
        return "轻微异常";
    }

    public String buildRiskDetail(int recentDays, long kickoutCount, long deviceCount, long ipCount) {
        return String.format("近%d天被踢下线%d次，涉及%d台设备、%d个IP",
                recentDays,
                kickoutCount,
                deviceCount,
                ipCount);
    }

    public boolean isAutoForbiddenReason(String forbidReason) {
        return AUTO_FORBID_REASON.equals(forbidReason);
    }
}
