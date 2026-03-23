package com.github.paicoding.forum.service.chatai.config;

import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigAdminReq;
import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigTestReq;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigAdminDTO;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigTestDTO;

/**
 * AI 配置管理服务
 *
 * @author Codex
 * @date 2026/3/23
 */
public interface AiConfigAdminService {
    /**
     * 读取当前生效中的 AI 配置
     *
     * @return AI 配置详情
     */
    AiConfigAdminDTO getConfig();

    /**
     * 保存 AI 配置
     *
     * @param req 配置内容
     */
    void save(AiConfigAdminReq req);

    /**
     * 测试指定模型连通性
     *
     * @param req 测试请求
     * @return 测试结果
     */
    AiConfigTestDTO test(AiConfigTestReq req);
}
