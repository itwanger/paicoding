package com.github.paicoding.forum.web.front.miniprogram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置。
 */
@Data
@Component
@ConfigurationProperties("paicoding.login.wx-mini")
public class WxMiniProgramProperties {
    /**
     * 小程序 AppID。
     */
    private String appId;

    /**
     * 小程序 AppSecret。
     */
    private String appSecret;

    /**
     * 本地开发兜底登录开关。生产环境必须关闭。
     */
    private Boolean mockEnabled = false;
}
