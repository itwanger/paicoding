package com.github.paicoding.forum.web.front.login.zsxq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YiHui
 * @date 2025/8/19
 */
@Component
@Data
@ConfigurationProperties("paicoding.login.zsxq")
public class ZsxqProperties {

    /**
     * 请求地址
     */
    private String api;

    /**
     * 应用id
     */
    private String appId;
    /**
     * 星球号
     */
    private String groupNumber;
    /**
     * 密钥
     */
    private String secret;
    /**
     * 回调地址
     */
    private String redirectUrl;
}
