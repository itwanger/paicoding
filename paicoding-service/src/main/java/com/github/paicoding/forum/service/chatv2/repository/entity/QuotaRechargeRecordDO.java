package com.github.paicoding.forum.service.chatv2.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配额充值记录表
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quota_recharge_record")
public class QuotaRechargeRecordDO extends BaseDO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 充值数量（tokens）
     */
    private Long rechargeAmount;

    /**
     * 充值前配额
     */
    private Long beforeQuota;

    /**
     * 充值后配额
     */
    private Long afterQuota;

    /**
     * 操作员ID（管理员）
     */
    private Long operatorId;

    /**
     * 操作员名称
     */
    private String operatorName;

    /**
     * 充值原因
     */
    private String reason;

    /**
     * 备注信息
     */
    private String remark;
}
