package com.github.paicoding.forum.web.openapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 开放平台相关配置
 *
 * @author yihui
 * @date 2025/9/15
 */
@Data
@ConfigurationProperties(prefix = "paicoding.openapi")
@Component
public class OpenApiProperties {

    /**
     * 授权的appId列表
     */
    private String appIds;


    /**
     * ip白名单
     */
    private String ipWhiteList;

    /**
     * oc的url
     */
    private String ocLoginRedirectUrl;


    public List<String> appIdList() {
        return Arrays.asList(appIds.split(","));
    }

    public List<String> ipWhiteList() {
        return Arrays.asList(ipWhiteList.split(","));
    }
}
