package com.github.paicoding.forum.api.model.vo.ai.config.dto;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 连通性测试结果
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class AiConfigTestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private AISourceEnum source;

    private Boolean success;

    private String message;

    private String answer;

    private Long costMs;
}
