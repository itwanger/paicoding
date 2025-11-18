package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

/**
 * 配额充值请求对象
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Data
public class QuotaRechargeReqVO {

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
     * 充值原因
     */
    private String reason;

    /**
     * 备注信息
     */
    private String remark;
}
