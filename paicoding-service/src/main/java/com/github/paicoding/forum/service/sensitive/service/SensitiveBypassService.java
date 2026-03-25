package com.github.paicoding.forum.service.sensitive.service;

/**
 * 敏感词处理豁免服务
 *
 * @author Codex
 * @date 2026/3/25
 */
public interface SensitiveBypassService {

    /**
     * 指定用户发布的内容是否跳过敏感词处理
     *
     * @param userId 用户id
     * @return true 表示跳过敏感词处理
     */
    boolean shouldBypassByUserId(Long userId);
}
