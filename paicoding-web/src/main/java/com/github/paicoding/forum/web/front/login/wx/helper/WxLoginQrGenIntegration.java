package com.github.paicoding.forum.web.front.login.wx.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hui.quick.plugin.base.Base64Util;
import com.github.hui.quick.plugin.base.DomUtil;
import com.github.hui.quick.plugin.base.constants.MediaType;
import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeGenV3;
import com.github.paicoding.forum.api.model.enums.login.LoginQrTypeEnum;
import com.github.paicoding.forum.api.model.exception.NoVlaInGuavaException;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2025/9/28
 */
@Slf4j
@Component
public class WxLoginQrGenIntegration {

    private static final String WX_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    private static final String WX_GEN_QR_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=";

    private final WxLoginProperties wxLoginProperties;

    //  对于服务号登录的场景

    private volatile WxAccessToken accessToken;


    /**
     * key = 验证码 value = 根据验证码生成的带参数服务号二维码
     */
    private LoadingCache<String, String> loginImgCache;

    public WxLoginQrGenIntegration(WxLoginProperties wxLoginProperties) {
        this.wxLoginProperties = wxLoginProperties;

        // 缓存五分钟，二维码的有效期为10分钟
        loginImgCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) {
                throw new NoVlaInGuavaException("no val: " + s);
            }
        });
    }

    public LoginQrTypeEnum getLoginQrType() {
        return LoginQrTypeEnum.valueOf(wxLoginProperties.getLoginQrType());
    }

    /**
     * 生成登录二维码
     *
     * @return
     */
    public String genLoginQrImg(String code) {
        LoginQrTypeEnum type = getLoginQrType();
        if (type == LoginQrTypeEnum.SERVICE_ACCOUNT) {
            // 服务号登录，首先获取带链接的二维码信息
            String qrText = genServiceAccountLoginQrCode(code);
            // 根据二维码内容生成二维码图片，返回给前端
            String base64Img = genQrImg(qrText);
            return DomUtil.toDomSrc(base64Img, MediaType.ImagePng);
        } else {
            // 普通公众号登录时
            return wxLoginProperties.getQrCodeImg();
        }
    }

    private String genQrImg(String qrText) {
        try {
            BufferedImage img;
            if (StringUtils.isBlank(wxLoginProperties.getQrCodeLogo())) {
                img = QrCodeGenV3.of(qrText).asImg();
            } else {
                img = QrCodeGenV3.of(qrText).setLogo(wxLoginProperties.getQrCodeLogo()).asImg();
            }
            return Base64Util.encode(img, "png");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAccessToken() {
        if (!checkAccessToken()) {
            synchronized (this) {
                if (!checkAccessToken()) {
                    refreshAccessToken();
                }
            }
        }

        if (accessToken == null || accessToken.accessToken == null) {
            log.error("获取微信AccessToken失败,accessToken为null");
            throw new RuntimeException("获取微信AccessToken失败");
        }
        return accessToken.accessToken;
    }

    /**
     * 校验token的有效性
     *
     * @return true 有效；false 失效
     */
    private boolean checkAccessToken() {
        return accessToken != null && accessToken.expireTimestamp > System.currentTimeMillis() + 60_000L;
    }

    private synchronized void refreshAccessToken() {
        WxAccessToken token = HttpRequestHelper.get(WX_TOKEN_URL,
                MapUtils.create("appid", wxLoginProperties.getAppId(), "secret", wxLoginProperties.getAppSecret()),
                WxAccessToken.class);

        if (token == null) {
            log.error("刷新微信AccessToken失败,API返回null");
            return;
        }
        if (StringUtils.isNotBlank(token.getErrCode()) && !"0".equals(token.getErrCode())) {
            log.error("刷新微信AccessToken失败,errCode={}, errMsg={}", token.getErrCode(), token.getErrMsg());
            return;
        }

        if (token.accessToken == null) {
            log.error("刷新微信AccessToken失败,accessToken为null, response={}", token);
            return;
        }

        accessToken = token;
        accessToken.expireTimestamp = System.currentTimeMillis() + token.expiresIn * 1000;
        log.info("刷新微信AccessToken成功,过期时间={}", accessToken.expireTimestamp);
    }

    /**
     * 生成服务号登录的二维码
     *
     * @return
     * @see <a href="https://developers.weixin.qq.com/doc/service/api/qrcode/qrcodes/api_createqrcode.html"/>
     */
    private String genServiceAccountLoginQrCode(String code) {
        // 同一个验证码的二维码可以进行缓存，避免重复调用；同时也可以提高接口时效
        String url = loginImgCache.getIfPresent(code);
        if (url != null) {
            return url;
        } else {
            String wxApiUrl = WX_GEN_QR_URL + getAccessToken();
            Map<String, Object> params = MapUtils.create("expire_seconds", 600, "action_name", "QR_SCENE");
            params.put("action_info", MapUtils.create("scene", MapUtils.create("scene_id", code, "scene_str", "paiLogin#" + code)));

            WxLoginQrCodeRes res = HttpRequestHelper.postJsonData(wxApiUrl, params, WxLoginQrCodeRes.class);

            // 检查响应是否有效
            if (res == null) {
                log.error("微信生成二维码API返回null,code={}", code);
                throw new RuntimeException("微信生成二维码失败:API返回null");
            }

            if (StringUtils.isNotBlank(res.getErrCode()) && !"0".equals(res.getErrCode())) {
                log.error("微信生成二维码API返回错误,code={}, errCode={}, errMsg={}", code, res.getErrCode(), res.getErrMsg());
                throw new RuntimeException("微信生成二维码失败:" + res.getErrMsg());
            }

            if (StringUtils.isBlank(res.url)) {
                log.error("微信生成二维码API返回的url为空,code={}, response={}", code, res);
                throw new RuntimeException("微信生成二维码失败:返回的url为空");
            }

            // 只有在url不为空时才放入缓存
            loginImgCache.put(code, res.url);
            return res.url;
        }
    }

    @Data
    private static class BaseWxRes {
        @JsonProperty("errcode")
        private String errCode;
        @JsonProperty("errmsg")
        private String errMsg;
    }

    @Data
    @ToString(callSuper = true)
    private static class WxAccessToken extends BaseWxRes {
        // 访问令牌
        @JsonProperty("access_token")
        private String accessToken;
        // 失效时间
        @JsonProperty("expires_in")
        private Integer expiresIn;
        // 令牌失效时间戳
        private Long expireTimestamp = 0L;
    }

    @Data
    @ToString(callSuper = true)
    private static class WxLoginQrCodeRes extends BaseWxRes {
        @JsonProperty("ticket")
        private String ticket;
        @JsonProperty("expire_seconds")
        private String expireSeconds;
        // 二维码内容，需要自己生成对应的二维码图片
        @JsonProperty("url")
        private String url;
    }
}
