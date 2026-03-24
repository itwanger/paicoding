package com.github.paicoding.forum.api.model.vo.config;

import lombok.Data;

/**
 * 敏感词操作请求
 *
 * @author Codex
 * @date 2026/3/24
 */
@Data
public class SensitiveWordOperateReq {
    /**
     * 目标词
     */
    private String word;
}
