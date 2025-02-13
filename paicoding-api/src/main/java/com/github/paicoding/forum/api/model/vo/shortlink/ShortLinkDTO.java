package com.github.paicoding.forum.api.model.vo.shortlink;

import lombok.Data;

/**
 * 短链接传输对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
public class ShortLinkDTO {
    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 短链接代码
     */
    private String shortCode;
}