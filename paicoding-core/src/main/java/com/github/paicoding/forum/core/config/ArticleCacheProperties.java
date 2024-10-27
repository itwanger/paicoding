package com.github.paicoding.forum.core.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: pai_coding
 * @description: 控制热点文章的缓存访问参数
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Data
@Valid
@ConfigurationProperties(prefix = "hot.article.cache")
public class ArticleCacheProperties {

    /**
     * 热点文章缓存的过期时间，默认为1天，单位为秒
     */
    private Long expireSeconds = 60 * 60 * 24L;

    /**
     * 热点文章缓存的最大数量，默认为10
     */
    private Integer maximumSize = 10;

}
