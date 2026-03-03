package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 异常严重级别枚举
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Getter
public enum ExceptionSeverity {
    /**
     * 严重：影响核心业务，需要立即处理（如数据库连接失败、支付失败等）
     */
    CRITICAL("CRITICAL", "🔴 严重", 1),

    /**
     * 高：影响重要功能，需要尽快处理（如OSS服务异常、短信发送失败等）
     */
    HIGH("HIGH", "🟠 高", 2),

    /**
     * 中：影响部分功能，需要关注（如缓存失败、非关键第三方服务异常等）
     */
    MEDIUM("MEDIUM", "🟡 中", 3),

    /**
     * 低：轻微影响，可后续处理（如日志记录失败等）
     */
    LOW("LOW", "🟢 低", 4);

    /**
     * 级别代码
     */
    private final String code;

    /**
     * 级别描述（带emoji）
     */
    private final String desc;

    /**
     * 优先级（数值越小越重要）
     */
    private final int priority;

    ExceptionSeverity(String code, String desc, int priority) {
        this.code = code;
        this.desc = desc;
        this.priority = priority;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 级别代码
     * @return 对应的枚举值，默认为MEDIUM
     */
    public static ExceptionSeverity fromCode(String code) {
        for (ExceptionSeverity severity : values()) {
            if (severity.code.equalsIgnoreCase(code)) {
                return severity;
            }
        }
        return MEDIUM;
    }
}
