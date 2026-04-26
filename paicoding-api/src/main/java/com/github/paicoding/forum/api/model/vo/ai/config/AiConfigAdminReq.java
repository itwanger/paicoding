package com.github.paicoding.forum.api.model.vo.ai.config;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import lombok.Data;

import java.util.List;

/**
 * AI 配置管理请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class AiConfigAdminReq {
    /**
     * 当前启用的模型源
     */
    private List<AISourceEnum> sources;

    private ChatGptConfig chatGpt;
    private ZhipuConfig zhipu;
    private ZhipuCodingConfig zhipuCoding;
    private XunFeiConfig xunFei;
    private DeepSeekConfig deepSeek;
    private DoubaoConfig doubao;
    private AliConfig ali;

    @Data
    public static class ChatGptConfig {
        private AISourceEnum main;
        private GptModelConfig gpt35;
        private GptModelConfig gpt4;
    }

    @Data
    public static class GptModelConfig {
        private List<String> keys;
        private Boolean proxy;
        private String apiHost;
        private Integer timeOut;
        private Integer maxToken;
    }

    @Data
    public static class ZhipuConfig {
        private String apiSecretKey;
        private String requestIdTemplate;
        private String model;
    }

    @Data
    public static class ZhipuCodingConfig {
        private String apiKey;
        private String apiHost;
        private String model;
        private Long timeout;
    }

    @Data
    public static class XunFeiConfig {
        private String hostUrl;
        private String domain;
        private String appId;
        private String apiKey;
        private String apiSecret;
        private String apiPassword;
    }

    @Data
    public static class DeepSeekConfig {
        private String apiKey;
        private String apiHost;
        private String model;
        private Long timeout;
    }

    @Data
    public static class DoubaoConfig {
        private String apiKey;
        private String apiHost;
        private String endPoint;
    }

    @Data
    public static class AliConfig {
        private String model;
    }
}
