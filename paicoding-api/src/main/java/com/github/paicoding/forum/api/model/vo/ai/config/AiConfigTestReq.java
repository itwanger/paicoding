package com.github.paicoding.forum.api.model.vo.ai.config;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import lombok.Data;

/**
 * AI 连通性测试请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class AiConfigTestReq {
    /**
     * 待测试模型
     */
    private AISourceEnum source;

    /**
     * 自定义测试提示词
     */
    private String prompt;
}
