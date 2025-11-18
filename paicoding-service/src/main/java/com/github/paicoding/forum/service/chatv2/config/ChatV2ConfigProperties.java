package com.github.paicoding.forum.service.chatv2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chat V2 配置属性
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat-v2")
public class ChatV2ConfigProperties {

    /**
     * 模型配置列表
     */
    private List<ModelConfig> models;

    /**
     * 默认模型 ID
     */
    private String defaultModel;

    /**
     * 单个模型配置
     */
    @Data
    public static class ModelConfig {
        /**
         * 模型唯一标识
         */
        private String id;

        /**
         * 模型显示名称
         */
        private String name;

        /**
         * 提供商名称
         */
        private String provider;

        /**
         * API 基础 URL
         */
        private String baseUrl;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 实际的模型名称（调用 API 时使用）
         */
        private String modelName;

        /**
         * 最大 token 数
         */
        private Integer maxTokens;

        /**
         * 温度参数
         */
        private Double temperature;

        /**
         * 模型描述
         */
        private String description;

        /**
         * 是否启用
         */
        private Boolean enabled;
    }
}
