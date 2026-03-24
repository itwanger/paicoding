package com.github.paicoding.forum.api.model.vo.config.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 敏感词命中统计
 *
 * @author Codex
 * @date 2026/3/24
 */
@Data
public class SensitiveWordHitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 命中的词
     */
    private String word;

    /**
     * 命中次数
     */
    private Integer hitCount;
}
