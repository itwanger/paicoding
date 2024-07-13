package com.github.paicoding.forum.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author XuYifei
 * @date 2024-07-12
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

    private String contactMeStarQrCode;

    private String zsxqUrl;

    private String zsxqPosterUrl;

    private String contactMeTitle;

    /**
     * 微信公众号登录url
     */
    private String wxLoginUrl;

    private String host;

    /**
     * 首次登录的欢迎信息
     */
    private String welcomeInfo;

    /**
     * 星球信息
     */
    private String starInfo;

    /**
     * oss的地址
     */
    private String oss;

    // 知识星球文章可阅读数
    private String zsxqArticleReadCount;

    // 需要登录文章可阅读数
    private String needLoginArticleReadCount;

    public String getOss() {
        if (oss == null) {
            this.oss = "";
        }
        return this.oss;
    }
}
