package com.github.paicoding.forum.service.sensitive.service;

import com.alibaba.fastjson.JSON;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词命中后的 AI 优化建议服务。
 * <p>
 * 核心目标不是“替用户改文案后偷偷发布”，而是在提交阶段给出一版更自然的改写建议，
 * 避免用户只能看到一串 ***** 却不知道该怎么调整。
 *
 * @author itwanger
 * @date 2026/3/13
 */
@Slf4j
@Service
public class SensitiveAiOptimizeService {

    private static final int CONTENT_PREVIEW_LIMIT = 220;
    private static final int SUGGESTION_LIMIT = 160;

    private static final String OPTIMIZE_PROMPT = "你是技术社区的内容润色助手。下面内容命中了平台敏感词，请在不改变原始核心意思的前提下，"
            + "改写成更克制、客观、适合公开发布的表达。\n"
            + "要求：\n"
            + "1. 只输出改写后的内容，不要解释，不要道歉，不要提敏感词、审核、平台规则\n"
            + "2. 保留原本的技术问题、观点或诉求\n"
            + "3. 删除引战、辱骂、色情、赌博、极端或其他高风险表达\n"
            + "4. 输出控制在%d字以内\n"
            + "场景：%s\n"
            + "命中的词：%s\n"
            + "原文：%s";

    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private DeepSeekIntegration deepSeekIntegration;

    @Autowired
    private ZhipuIntegration zhipuIntegration;

    public void validateCommentOrThrow(String content) {
        SensitiveAdvice advice = analyze(content, SensitiveScene.COMMENT);
        if (!advice.isPass()) {
            throw ExceptionUtil.of(StatusEnum.SENSITIVE_CONTENT, advice.getUserMessage());
        }
    }

    public void validateArticleOrThrow(String title, String summary, String content) {
        String articleSnapshot = buildArticleSnapshot(title, summary, content);
        SensitiveAdvice advice = analyze(articleSnapshot, SensitiveScene.ARTICLE);
        if (!advice.isPass()) {
            throw ExceptionUtil.of(StatusEnum.SENSITIVE_CONTENT, advice.getUserMessage());
        }
    }

    public String buildChatBlockMessage(String question) {
        SensitiveAdvice advice = analyze(question, SensitiveScene.CHAT);
        if (advice.isPass()) {
            return null;
        }
        return advice.getUserMessage();
    }

    public String buildChatBlockMessage(AISourceEnum source, String question) {
        return buildChatBlockMessage(question);
    }

    private SensitiveAdvice analyze(String text, SensitiveScene scene) {
        if (StringUtils.isBlank(text)) {
            return SensitiveAdvice.pass();
        }

        List<String> hitWords = sensitiveService.contains(text);
        if (hitWords == null || hitWords.isEmpty()) {
            return SensitiveAdvice.pass();
        }

        String optimized = optimizeWithAi(text, hitWords, scene);
        if (StringUtils.isBlank(optimized)) {
            optimized = fallbackSuggestion(text, hitWords, scene);
        }

        String suggestion = trimToLimit(cleanupAiAnswer(optimized), SUGGESTION_LIMIT);
        String userMessage = buildUserMessage(scene, suggestion);
        return SensitiveAdvice.blocked(hitWords, suggestion, userMessage);
    }

    private String optimizeWithAi(String text, List<String> hitWords, SensitiveScene scene) {
        String preview = trimToLimit(text, CONTENT_PREVIEW_LIMIT);
        String prompt = String.format(OPTIMIZE_PROMPT, SUGGESTION_LIMIT, scene.getDesc(), joinHitWords(hitWords), preview);

        try {
            ChatItemVo chatItem = new ChatItemVo().initQuestion(prompt);
            if (deepSeekIntegration.directReturn(chatItem) && StringUtils.isNotBlank(chatItem.getAnswer())) {
                return chatItem.getAnswer();
            }
        } catch (Exception e) {
            log.warn("DeepSeek 敏感词改写建议生成失败, scene={}", scene, e);
        }

        try {
            ChatItemVo chatItem = new ChatItemVo().initQuestion(prompt);
            if (zhipuIntegration.directReturn(0L, chatItem) && StringUtils.isNotBlank(chatItem.getAnswer())) {
                return chatItem.getAnswer();
            }
        } catch (Exception e) {
            log.warn("智谱 敏感词改写建议生成失败, scene={}", scene, e);
        }

        return "";
    }

    private String fallbackSuggestion(String text, List<String> hitWords, SensitiveScene scene) {
        String sanitized = trimToLimit(text, CONTENT_PREVIEW_LIMIT);
        List<String> sortedWords = hitWords.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());
        for (String word : sortedWords) {
            sanitized = StringUtils.replace(sanitized, word, "相关表述");
        }
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        if (StringUtils.isBlank(sanitized)) {
            sanitized = scene == SensitiveScene.ARTICLE
                    ? "请围绕技术事实、问题现象和解决方案重新组织表述。"
                    : "请删除过激或违规表达，改成更客观的说法。";
        }

        return sanitized;
    }

    private String buildUserMessage(SensitiveScene scene, String suggestion) {
        StringBuilder builder = new StringBuilder("内容包含敏感表达，已拦截提交。");
        if (StringUtils.isNotBlank(suggestion)) {
            builder.append(" 你可以参考这版改写：").append(suggestion);
        } else if (scene == SensitiveScene.ARTICLE) {
            builder.append(" 建议保留技术事实，删除情绪化或违规词汇后再发布。");
        } else {
            builder.append(" 建议删除过激或违规词汇后再试。");
        }

        if (scene == SensitiveScene.CHAT) {
            builder.append(" 如果是误伤，也可以联系管理员加入白名单。");
        }
        return builder.toString();
    }

    private String buildArticleSnapshot(String title, String summary, String content) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(title)) {
            builder.append("标题：").append(title).append('\n');
        }
        if (StringUtils.isNotBlank(summary)) {
            builder.append("摘要：").append(summary).append('\n');
        }
        if (StringUtils.isNotBlank(content)) {
            builder.append("正文摘录：").append(trimToLimit(content, CONTENT_PREVIEW_LIMIT));
        }
        return builder.toString();
    }

    private String joinHitWords(List<String> hitWords) {
        if (hitWords == null || hitWords.isEmpty()) {
            return "";
        }
        return hitWords.stream().distinct().collect(Collectors.joining("、"));
    }

    private String cleanupAiAnswer(String answer) {
        if (StringUtils.isBlank(answer)) {
            return "";
        }

        String cleaned = answer.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            try {
                cleaned = JSON.parseObject(cleaned, String.class);
            } catch (Exception ignore) {
                log.debug("敏感词建议 JSON 反序列化失败, answer={}", answer);
            }
        }

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(text|markdown)?\\s*", "")
                    .replaceFirst("```\\s*$", "");
        }

        return cleaned.replaceAll("\\s+", " ").trim();
    }

    private String trimToLimit(String text, int limit) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= limit) {
            return normalized;
        }
        return normalized.substring(0, limit) + "...";
    }

    @Data
    @AllArgsConstructor
    private static class SensitiveAdvice {
        private boolean pass;
        private List<String> hitWords;
        private String suggestion;
        private String userMessage;

        static SensitiveAdvice pass() {
            return new SensitiveAdvice(true, Collections.emptyList(), "", "");
        }

        static SensitiveAdvice blocked(List<String> hitWords, String suggestion, String userMessage) {
            return new SensitiveAdvice(false, hitWords, suggestion, userMessage);
        }
    }

    private enum SensitiveScene {
        COMMENT("评论"),
        ARTICLE("文章"),
        CHAT("AI提问");

        private final String desc;

        SensitiveScene(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
