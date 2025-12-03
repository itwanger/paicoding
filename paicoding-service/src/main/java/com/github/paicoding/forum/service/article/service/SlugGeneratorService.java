package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.chatgpt.ChatGptIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * URL Slug AI生成服务
 * 使用AI从文章标题中提取关键词并翻译为英文
 *
 * @author YiHui
 * @date 2025/12/03
 */
@Slf4j
@Service
public class SlugGeneratorService {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATE_DASH = Pattern.compile("-+");
    private static final int MAX_SLUG_LENGTH = 50;

    @Autowired(required = false)
    private ZhipuIntegration zhipuIntegration;

    @Autowired(required = false)
    private ChatGptIntegration chatGptIntegration;

    @Autowired(required = false)
    private XunFeiIntegration xunFeiIntegration;

    @Autowired(required = false)
    private DeepSeekIntegration deepSeekIntegration;

    /**
     * 使用AI生成URL slug
     * 直接调用AI接口，不经过用户额度检查
     *
     * @param title 文章标题
     * @return URL友好的slug
     */
    public String generateSlugWithAI(String title) {
        if (StringUtils.isBlank(title)) {
            return "";
        }

        String prompt = buildPrompt(title);
        ChatItemVo chatItem = new ChatItemVo();
        chatItem.setQuestion(prompt);

        if (zhipuIntegration != null) {
            try {
                zhipuIntegration.directReturn(0L, chatItem);
                if (StringUtils.isNotBlank(chatItem.getAnswer())) {
                    String slug = cleanAIResponse(chatItem.getAnswer());
                    if (StringUtils.isNotBlank(slug) && isValidSlug(slug)) {
                        log.info("Generated slug with ZhipuAI for title '{}': {}", title, slug);
                        return slug;
                    }
                }
            } catch (Exception e) {
                log.warn("ZhipuAI failed, trying next AI service: {}", e.getMessage());
            }
        }

        if (chatGptIntegration != null) {
            try {
                chatItem = new ChatItemVo();
                chatItem.setQuestion(prompt);
                chatGptIntegration.directReturn(0L, chatItem);
                if (StringUtils.isNotBlank(chatItem.getAnswer())) {
                    String slug = cleanAIResponse(chatItem.getAnswer());
                    if (StringUtils.isNotBlank(slug) && isValidSlug(slug)) {
                        log.info("Generated slug with ChatGPT for title '{}': {}", title, slug);
                        return slug;
                    }
                }
            } catch (Exception e) {
                log.warn("ChatGPT failed, trying next AI service: {}", e.getMessage());
            }
        }

        if (deepSeekIntegration != null) {
            try {
                chatItem = new ChatItemVo();
                chatItem.setQuestion(prompt);
                deepSeekIntegration.directReturn(chatItem);
                if (StringUtils.isNotBlank(chatItem.getAnswer())) {
                    String slug = cleanAIResponse(chatItem.getAnswer());
                    if (StringUtils.isNotBlank(slug) && isValidSlug(slug)) {
                        log.info("Generated slug with DeepSeek for title '{}': {}", title, slug);
                        return slug;
                    }
                }
            } catch (Exception e) {
                log.warn("DeepSeek failed: {}", e.getMessage());
            }
        }

        log.warn("All AI services failed, using fallback slug for title: {}", title);
        return generateFallbackSlug(title);
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(String title) {
        return "任务：提取文章标题的核心关键词（1-2个），转为简短英文slug\n" +
                "要求：\n" +
                "- 只保留最核心的技术词汇或动作词\n" +
                "- 人名、修饰词全部忽略\n" +
                "- 翻译成英文，小写，用-连接\n" +
                "- 总长度控制在2-3个单词内\n" +
                "- 只返回slug，无任何解释\n\n" +
                "特殊词汇映射：\n" +
                "- 技术派 => paicoding\n" +
                "- 派聪明 => paismart\n\n" +
                "示例：\n" +
                "沉默王二的Java教程 => java-tutorial\n" +
                "我要学习Spring Boot => spring-boot\n" +
                "深入理解JVM虚拟机原理 => jvm-principle\n" +
                "Redis性能优化技巧分享 => redis-optimization\n" +
                "沉默王二很牛逼，我要引流了 => traffic-guide\n" +
                "技术派社区使用指南 => paicoding-guide\n" +
                "派聪明AI助手介绍 => paismart-intro\n\n" +
                "标题：" + title + "\n" +
                "Slug：";
    }

    /**
     * 清理AI返回的响应
     */
    private String cleanAIResponse(String aiResponse) {
        if (StringUtils.isBlank(aiResponse)) {
            return "";
        }

        // 移除可能的前后缀说明文字
        String cleaned = aiResponse.trim();
        
        // 提取可能的slug（查找符合格式的部分）
        String[] lines = cleaned.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 跳过空行和明显的说明文字
            if (StringUtils.isBlank(line) || line.contains("：") || line.contains(":") 
                    || line.contains("标题") || line.length() < 3) {
                continue;
            }
            // 清理可能的引号
            line = line.replaceAll("[\"'`]", "");
            // 转小写
            line = line.toLowerCase();
            // 只保留字母、数字和连字符
            line = line.replaceAll("[^a-z0-9-]", "");
            
            if (StringUtils.isNotBlank(line) && line.matches("^[a-z0-9-]+$")) {
                return limitLength(line);
            }
        }

        // 如果没找到合适的，对整个响应做清理
        cleaned = cleaned.toLowerCase();
        cleaned = cleaned.replaceAll("[^a-z0-9-\\s]", "");
        cleaned = WHITESPACE.matcher(cleaned).replaceAll("-");
        cleaned = DUPLICATE_DASH.matcher(cleaned).replaceAll("-");
        cleaned = cleaned.replaceAll("^-+|-+$", "");

        return limitLength(cleaned);
    }

    /**
     * 限制slug长度
     */
    private String limitLength(String slug) {
        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH);
            int lastDash = slug.lastIndexOf('-');
            if (lastDash > 0) {
                slug = slug.substring(0, lastDash);
            }
        }
        return slug;
    }

    /**
     * 验证slug格式
     */
    private boolean isValidSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return false;
        }
        return slug.matches("^[a-z0-9-]+$") && slug.length() <= MAX_SLUG_LENGTH;
    }

    /**
     * 降级方案：简单的slug生成
     */
    private String generateFallbackSlug(String title) {
        return "article-" + System.currentTimeMillis();
    }
}