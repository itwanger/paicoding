package com.github.paicoding.forum.api.model.vo.shortlink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接传输对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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