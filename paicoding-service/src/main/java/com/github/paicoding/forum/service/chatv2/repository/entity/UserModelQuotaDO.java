package com.github.paicoding.forum.service.chatv2.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户模型配额表
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_model_quota")
public class UserModelQuotaDO extends BaseDO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型ID（如：gpt-4o, deepseek-chat）
     */
    private String modelId;

    /**
     * 总配额（tokens）
     */
    private Long totalQuota;

    /**
     * 已使用配额（tokens）
     */
    private Long usedQuota;

    /**
     * 剩余配额（tokens）
     * 冗余字段：remaining_quota = total_quota - used_quota
     */
    private Long remainingQuota;

    /**
     * 累计使用量（tokens）
     * 用于统计，不会因充值而重置
     */
    private Long totalUsed;

    /**
     * 最后使用时间
     */
    private Date lastUsedTime;
}
