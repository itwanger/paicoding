package com.github.liuyueyi.forum.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yihui
 * @date 2022/6/15
 */
@Data
@ConfigurationProperties(prefix = "view.site")
@Component
public class GlobalViewConfig {

    private String cdnImgStyle;

    private String websiteRecord;

    private Integer pageSize;

    private String websiteName;

    private String websiteLogoUrl;

    private String websiteFaviconIconUrl;

    private String contactMeWxQrCode;

    private String contactMeTitle;

    /**
     * 微信公众号登录url
     */
    private String wxLoginUrl;

    private String host;
}
