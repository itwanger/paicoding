package com.github.paicoding.forum.api.model.vo.config;

import lombok.Data;

import java.util.List;

/**
 * 敏感词配置请求
 *
 * @author Codex
 * @date 2026/3/24
 */
@Data
public class SensitiveWordConfigReq {
    /**
     * 是否开启敏感词能力
     */
    private Boolean enable;

    /**
     * 敏感词列表
     */
    private List<String> denyWords;

    /**
     * 白名单列表
     */
    private List<String> allowWords;
}
