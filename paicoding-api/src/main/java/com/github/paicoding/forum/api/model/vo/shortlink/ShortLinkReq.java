package com.github.paicoding.forum.api.model.vo.shortlink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接请求对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkReq {
    /**
     * 原始URL
     */
    private String originalUrl;
}