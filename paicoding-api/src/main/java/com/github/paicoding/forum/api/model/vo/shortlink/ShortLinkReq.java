package com.github.paicoding.forum.api.model.vo.shortlink;

import lombok.Data;

/**
 * 短链接请求对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
public class ShortLinkReq {
    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 用户ID
     */
    private String userId;
}