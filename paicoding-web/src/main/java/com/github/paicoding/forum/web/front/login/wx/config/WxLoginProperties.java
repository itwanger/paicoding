package com.github.paicoding.forum.web.front.login.wx.config;

import com.github.paicoding.forum.api.model.enums.login.LoginQrTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YiHui
 * @date 2025/8/19
 */
@Component
@Data
@ConfigurationProperties("paicoding.login.wx")
public class WxLoginProperties {
    /**
     * 登录二维码的类型：微信公众号、服务号
     *
     * @see LoginQrTypeEnum#name()
     */
    private String loginQrType;

    /**
     * 开发者ID(AppID)
     */
    private String appId;
    /**
     * 开发者密码
     */
    private String appSecret;
    /**
     * 如果是普通公众号登录，这里为公众号的二维码图片地址
     */
    private String qrCodeImg;

    /**
     * 二维码的logo，适用于服务号的场景
     */
    private String qrCodeLogo;

    /**
     * 微信公众号安全校验token，为后台配置
     */
    private String securityCheckToken;
}
