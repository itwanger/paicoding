package com.github.paicoding.forum.service.pay.config;

import com.github.hui.quick.plugin.base.file.FileReadUtil;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 微信支付配置
 *
 * @author YiHui
 * @date 2024/12/3
 */
@Data
@Component
@ConditionalOnProperty(value = "wx.pay.enable")
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayConfig {
    //APPID
    private String appId;
    //mchid
    private String merchantId;
    //商户API私钥
    private String privateKey;
    //商户证书序列号
    private String merchantSerialNumber;
    //商户APIv3密钥
    private String apiV3Key;
    //支付通知地址
    private String payNotifyUrl;
    //退款通知地址
    private String refundNotifyUrl;

    /**
     * 获取私钥信息
     *
     * @return 私钥内容
     */
    public String getPrivateKeyContent() {
        try {
            return FileReadUtil.readAll(privateKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
