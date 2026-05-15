package com.github.paicoding.forum.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云视频点播配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vod.ali")
public class AliVodProperties {
    /**
     * 是否启用阿里云 VOD 上传。
     */
    private Boolean enabled = false;

    /**
     * VOD 接入地域，如 cn-shanghai。
     */
    private String regionId = "cn-shanghai";

    /**
     * 前端上传 SDK 初始化需要的用户标识，有值即可。
     */
    private String userId = "paicoding";

    private String accessKeyId;

    private String accessKeySecret;

    private String storageLocation;

    private String cateId;

    private String templateGroupId;

    private String workflowId;
}
