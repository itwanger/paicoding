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

    /**
     * 技术派站内旧版 AI 聊天入口名称
     */
    private String paiChatName;

    /**
     * 技术派站外新版派聪明入口名称
     */
    private String paiSmartName;

    /**
     * 新版派聪明跳转地址
     */
    private String paiSmartUrl;

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

    // 星球专享内容未解锁时的试看字数
    private String zsxqArticleReadCount;

    // 需要登录内容未登录时的试看字数
    private String needLoginArticleReadCount;

    // 付费内容未支付时的试看字数
    private String needPayArticleReadCount;

    // 专栏首页跑马灯公告
    private String columnInfoNews;

    public String getOss() {
        if (oss == null) {
            this.oss = "";
        }
        return this.oss;
    }

    public GlobalViewConfig setOss(String oss) {
        this.oss = oss;
        CdnUtil.setSiteOssPrefix(oss);
        return this;
    }

    public String getPaiChatName() {
        return paiChatName == null || paiChatName.trim().isEmpty() ? "PaiChat" : paiChatName;
    }

    public String getPaiSmartName() {
        return paiSmartName == null || paiSmartName.trim().isEmpty() ? "PaiSmart" : paiSmartName;
    }

    public String getPaiSmartUrl() {
        return paiSmartUrl == null || paiSmartUrl.trim().isEmpty() ? "http://localhost:9527/" : paiSmartUrl;
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
