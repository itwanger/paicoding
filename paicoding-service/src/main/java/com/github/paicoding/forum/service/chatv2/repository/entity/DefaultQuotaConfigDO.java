package com.github.paicoding.forum.service.chatv2.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 默认配额配置表
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("default_quota_config")
public class DefaultQuotaConfigDO extends BaseDO {

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型显示名称
     */
    private String modelName;

    /**
     * 新用户默认配额（tokens）
     */
    private Long defaultQuota;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Integer enabled;

    /**
     * 优先级，数字越大优先级越高
     */
    private Integer priority;

    /**
     * 配置描述
     */
    private String description;
}
