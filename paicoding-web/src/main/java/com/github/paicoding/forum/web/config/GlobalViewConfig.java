package com.github.paicoding.forum.web.config;

import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
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

    /**
     * true 表示开启了微信支付
     * false 表示未配置微信支付
     */
    private Boolean wxPayEnable;

    private String cdnImgStyle;

    private String websiteRecord;

    private Integer pageSize;

    private String websiteName;

    private String websiteLogoUrl;

    private String websiteFaviconIconUrl;

    private String contactMeWxQrCode;

    private String contactMeStarQrCode;

    /**
     * 知识星球的跳转地址
     */
    private String zsxqUrl;

    /**
     * 知识星球首页的一个展示图片地址
     */
    private String zsxqImgUrl;

    /**
     * 知识星球二维码的地址，派聪明 AI助手用
     */
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

    // 需要支付的可阅读数
    private String needPayArticleReadCount;

    // 专栏首页跑马灯公告
    private String columnInfoNews;

    public String getOss() {
        if (oss == null) {
            this.oss = "";
        }
        return this.oss;
    }


    public GlobalViewConfig setWebsiteLogoUrl(String websiteLogoUrl) {
        this.websiteLogoUrl = CdnUtil.autoTransCdn(websiteLogoUrl);
        return this;
    }

    public GlobalViewConfig setWebsiteFaviconIconUrl(String websiteFaviconIconUrl) {
        this.websiteFaviconIconUrl = CdnUtil.autoTransCdn(websiteFaviconIconUrl);
        return this;
    }

    public GlobalViewConfig setZsxqPosterUrl(String zsxqPosterUrl) {
        this.zsxqPosterUrl = CdnUtil.autoTransCdn(zsxqPosterUrl);
        return this;
    }

    public GlobalViewConfig setZsxqImgUrl(String zsxqImgUrl) {
        this.zsxqImgUrl = CdnUtil.autoTransCdn(zsxqImgUrl);
        return this;
    }

    public GlobalViewConfig setContactMeWxQrCode(String contactMeWxQrCode) {
        this.contactMeWxQrCode = CdnUtil.autoTransCdn(contactMeWxQrCode);
        return this;
    }

    public GlobalViewConfig setContactMeStarQrCode(String contactMeStarQrCode) {
        this.contactMeStarQrCode = CdnUtil.autoTransCdn(contactMeStarQrCode);
        return this;
    }
}
