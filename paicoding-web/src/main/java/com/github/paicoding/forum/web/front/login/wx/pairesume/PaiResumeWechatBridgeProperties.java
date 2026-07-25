package com.github.paicoding.forum.web.front.login.wx.pairesume;

import com.github.paicoding.forum.core.util.EnvUtil;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * 派简历复用派聪明服务号时的受信桥接配置。
 *
 * <p>此配置默认关闭；一旦开启，二维码生成与微信事件转发必须同时完整配置，
 * 不允许退化为无签名调用。</p>
 */
@Data
@Component
@ConfigurationProperties("paicoding.login.wx.pai-resume")
public class PaiResumeWechatBridgeProperties {
    private static final Pattern SCENE_PREFIX = Pattern.compile("[A-Za-z0-9_-]{2,16}");

    private final WxLoginProperties wxLoginProperties;

    private boolean enabled;
    private String callbackUrl;
    private String bridgeSecret;
    private String scenePrefix = "pr_";
    private int clockSkewSeconds = 300;
    private int replayTtlSeconds = 600;
    private int connectTimeoutMillis = 3000;
    private int readTimeoutMillis = 5000;

    @PostConstruct
    public void validateAtStartup() {
        if (enabled) {
            requireReady();
        }
    }

    public void requireReady() {
        if (!enabled) {
            throw new IllegalStateException("PaiResume WeChat bridge is disabled");
        }
        if (StringUtils.isBlank(callbackUrl)
                || StringUtils.isBlank(bridgeSecret) || bridgeSecret.length() < 32
                || !SCENE_PREFIX.matcher(StringUtils.defaultString(scenePrefix)).matches()
                || clockSkewSeconds < 30 || clockSkewSeconds > 600
                || replayTtlSeconds < clockSkewSeconds
                || connectTimeoutMillis < 500 || connectTimeoutMillis > 15000
                || readTimeoutMillis < 500 || readTimeoutMillis > 15000
                || StringUtils.isBlank(wxLoginProperties.getAppId())
                || StringUtils.isBlank(wxLoginProperties.getSecurityCheckToken())) {
            throw new IllegalStateException("PaiResume WeChat bridge configuration is incomplete or unsafe");
        }

        URI uri = URI.create(callbackUrl.trim());
        if (StringUtils.isBlank(uri.getScheme()) || StringUtils.isBlank(uri.getHost())
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || (EnvUtil.isPro() && !"https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalStateException("PaiResume WeChat callback URL is invalid");
        }
    }

    public String normalizedCallbackUrl() {
        return callbackUrl == null ? "" : callbackUrl.trim();
    }
}
