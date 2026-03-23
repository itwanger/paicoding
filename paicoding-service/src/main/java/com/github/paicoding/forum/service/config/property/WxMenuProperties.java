package com.github.paicoding.forum.service.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信菜单配置
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
@Component
@ConfigurationProperties(prefix = "paicoding.login.wx")
public class WxMenuProperties {
    private String appId;
    private String appSecret;
}
