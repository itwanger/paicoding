package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

/**
 * 模型信息视图对象
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
public class ModelInfoVO {

    /**
     * 模型ID
     */
    private String id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 提供商
     */
    private String provider;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 最大 Token 数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private Double temperature;
}
