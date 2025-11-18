package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

import java.util.Date;

/**
 * 用户模型配额视图对象
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Data
public class UserModelQuotaVO {

    /**
     * 配额ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

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
     */
    private Long remainingQuota;

    /**
     * 累计使用量（tokens）
     */
    private Long totalUsed;

    /**
     * 使用率（百分比）
     */
    private Double usageRate;

    /**
     * 最后使用时间
     */
    private Date lastUsedTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
