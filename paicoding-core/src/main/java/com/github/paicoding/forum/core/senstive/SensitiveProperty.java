package com.github.paicoding.forum.core.senstive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词相关配置，db配置表中的配置优先级更高，支持动态刷新
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Component
@ConfigurationProperties(prefix = SensitiveProperty.SENSITIVE_KEY_PREFIX)
public class SensitiveProperty {
    public static final String SENSITIVE_KEY_PREFIX = "paicoding.sensitive";
    /**
     * true 表示开启敏感词校验
     */
    private Boolean enable;

    /**
     * 自定义的敏感词
     */
    private List<String> deny;

    /**
     * 自定义的非敏感词
     */
    private List<String> allow;
}
