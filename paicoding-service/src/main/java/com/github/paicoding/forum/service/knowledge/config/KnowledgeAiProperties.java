package com.github.paicoding.forum.service.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "knowledge-ai")
public class KnowledgeAiProperties {
    private String baseUrl;
    private String apiKey;
    private String modelName = "qwen3.5-plus";
    private Integer maxTokens = 4096;
    private Double temperature = 0.3;
}
