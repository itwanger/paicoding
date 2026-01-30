package com.github.paicoding.forum.service.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateRes;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.service.article.service.AiSeoService;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI SEO生成服务实现
 *
 * @author 沉默王二
 * @date 2026/1/28
 */
@Slf4j
@Service
public class AiSeoServiceImpl implements AiSeoService {
    
    @Autowired
    private ZhipuIntegration zhipuIntegration;
    
    @Autowired
    private DeepSeekIntegration deepSeekIntegration;
    
    private static final String SEO_PROMPT_TEMPLATE = 
            "你是一个专业的SEO优化专家。根据以下信息生成SEO优化的标题和描述：\n\n" +
            "短标题：%s\n" +
            "正文内容：%s\n\n" +
            "要求：\n" +
            "1. 标题(title)：基于短标题扩展，长度控制在50-60个字符，包含关键词，吸引点击\n" +
            "2. 描述(description)：总结文章核心内容，长度控制在150-160个字符，自然融入关键词\n" +
            "3. 两者都要符合SEO最佳实践，提高搜索引擎排名\n\n" +
            "重要：请严格按照以下JSON格式返回，不要添加任何额外说明文字、代码块标记或换行符：\n" +
            "{\"title\":\"生成的SEO标题\",\"description\":\"生成的SEO描述\"}";
    
    @Override
    public AiSeoGenerateRes generateSeoTitleAndDescription(AiSeoGenerateReq req) {
        if (StringUtils.isBlank(req.getShortTitle()) || StringUtils.isBlank(req.getContent())) {
            throw new IllegalArgumentException("短标题和正文内容不能为空");
        }
        
        // 截取正文前400字
        String content = req.getContent();
        if (content.length() > 400) {
            content = content.substring(0, 400);
        }
        
        // 构建提示词
        String prompt = String.format(SEO_PROMPT_TEMPLATE, req.getShortTitle(), content);
        
        String answer = null;
        String aiSource = null;
        
        // 优先尝试智谱AI
        try {
            log.info("尝试使用智谱AI生成SEO内容");
            ChatItemVo chatItem = new ChatItemVo().initQuestion(prompt);
            boolean success = zhipuIntegration.directReturn(0L, chatItem);
            
            if (success && StringUtils.isNotBlank(chatItem.getAnswer())) {
                answer = chatItem.getAnswer();
                aiSource = "ZhiPu";
                log.info("智谱AI调用成功");
            } else {
                log.warn("智谱AI调用失败，尝试使用DeepSeek");
            }
        } catch (Exception e) {
            log.error("智谱AI调用异常，尝试使用DeepSeek", e);
        }
        
        // 如果智谱AI失败，尝试DeepSeek
        if (StringUtils.isBlank(answer)) {
            try {
                log.info("使用DeepSeek生成SEO内容");
                ChatItemVo chatItem = new ChatItemVo().initQuestion(prompt);
                boolean success = deepSeekIntegration.directReturn(chatItem);
                
                if (success && StringUtils.isNotBlank(chatItem.getAnswer())) {
                    answer = chatItem.getAnswer();
                    aiSource = "DeepSeek";
                    log.info("DeepSeek调用成功");
                } else {
                    log.error("DeepSeek调用失败");
                }
            } catch (Exception e) {
                log.error("DeepSeek调用异常", e);
            }
        }
        
        // 如果AI调用都失败，返回兜底方案
        if (StringUtils.isBlank(answer)) {
            log.warn("所有AI服务调用失败，使用兜底方案");
            return createFallbackResponse(req);
        }
        
        try {
            // 根据AI源选择不同的解析方法
            AiSeoGenerateRes result;
            if ("ZhiPu".equals(aiSource)) {
                result = parseZhipuResponse(answer);
            } else {
                result = parseDeepSeekResponse(answer);
            }
            
            // 验证结果
            if (StringUtils.isBlank(result.getTitle()) || StringUtils.isBlank(result.getDescription())) {
                log.error("AI生成的标题或描述为空，使用兜底方案");
                return createFallbackResponse(req);
            }
            
            log.info("AI生成SEO内容成功，短标题：{}，生成标题：{}", req.getShortTitle(), result.getTitle());
            return result;
            
        } catch (Exception e) {
            log.error("解析AI响应失败，使用兜底方案", e);
            return createFallbackResponse(req);
        }
    }
    
    /**
     * 解析智谱AI返回的JSON响应
     * 智谱AI的content被JsonUtil.toStr()序列化了一次，需要先反序列化
     */
    private AiSeoGenerateRes parseZhipuResponse(String answer) {
        log.info("智谱AI原始响应: {}", answer);
        
        try {
            String cleanedAnswer = answer.trim();
            
            // 智谱AI的content被JsonUtil.toStr()序列化了一次，导致JSON字符串被转义
            // 例如: "{\"title\":\"xxx\"}" 需要先反序列化成真正的JSON字符串
            if (cleanedAnswer.startsWith("\"") && cleanedAnswer.endsWith("\"")) {
                // 反序列化一次
                cleanedAnswer = JSON.parseObject(cleanedAnswer, String.class);
                log.info("智谱AI反序列化后: {}", cleanedAnswer);
            }
            
            // 移除可能的换行符
            cleanedAnswer = cleanedAnswer.replaceAll("\\n", "")
                                         .replaceAll("\\r", "")
                                         .trim();
            
            // 解析JSON
            AiSeoGenerateRes result = JSON.parseObject(cleanedAnswer, AiSeoGenerateRes.class);
            if (result != null && StringUtils.isNotBlank(result.getTitle()) && StringUtils.isNotBlank(result.getDescription())) {
                log.info("智谱AI JSON解析成功");
                return result;
            }
        } catch (Exception e) {
            log.warn("智谱AI JSON解析失败: {}", e.getMessage());
        }
        
        // 解析失败，返回原始内容
        log.warn("智谱AI无法解析为标准JSON格式，返回原始响应");
        AiSeoGenerateRes fallback = new AiSeoGenerateRes();
        fallback.setTitle(answer);
        fallback.setDescription("AI返回内容格式异常，请手动调整");
        return fallback;
    }
    
    /**
     * 解析DeepSeek返回的JSON响应
     * DeepSeek直接返回JSON对象，不需要反序列化
     */
    private AiSeoGenerateRes parseDeepSeekResponse(String answer) {
        log.info("DeepSeek原始响应: {}", answer);
        
        try {
            String cleanedAnswer = answer.trim();
            
            // DeepSeek可能会返回Markdown代码块格式
            if (cleanedAnswer.startsWith("```")) {
                cleanedAnswer = cleanedAnswer.replaceFirst("^```(json)?\\s*", "")
                                             .replaceFirst("```\\s*$", "");
            }
            
            // 移除换行符
            cleanedAnswer = cleanedAnswer.replaceAll("\\n", "")
                                         .replaceAll("\\r", "")
                                         .trim();
            
            log.info("DeepSeek清理后: {}", cleanedAnswer);
            
            // 解析JSON
            AiSeoGenerateRes result = JSON.parseObject(cleanedAnswer, AiSeoGenerateRes.class);
            if (result != null && StringUtils.isNotBlank(result.getTitle()) && StringUtils.isNotBlank(result.getDescription())) {
                log.info("DeepSeek JSON解析成功");
                return result;
            }
            
            // 尝试提取{}之间的内容
            int jsonStart = cleanedAnswer.indexOf("{");
            int jsonEnd = cleanedAnswer.lastIndexOf("}");
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = cleanedAnswer.substring(jsonStart, jsonEnd + 1);
                log.info("DeepSeek提取JSON: {}", jsonStr);
                result = JSON.parseObject(jsonStr, AiSeoGenerateRes.class);
                if (result != null && StringUtils.isNotBlank(result.getTitle()) && StringUtils.isNotBlank(result.getDescription())) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("DeepSeek JSON解析失败: {}", e.getMessage());
        }
        
        // 解析失败，返回原始内容
        log.warn("DeepSeek无法解析为标准JSON格式，返回原始响应");
        AiSeoGenerateRes fallback = new AiSeoGenerateRes();
        fallback.setTitle(answer);
        fallback.setDescription("AI返回内容格式异常，请手动调整");
        return fallback;
    }
    
    /**
     * 当AI调用失败时的兜底方案
     */
    private AiSeoGenerateRes createFallbackResponse(AiSeoGenerateReq req) {
        AiSeoGenerateRes response = new AiSeoGenerateRes();
        
        // 基于短标题生成简单的长标题
        String title = req.getShortTitle();
        if (title.length() < 40) {
            title = title + " - 详细教程与实践指南";
        }
        response.setTitle(title);
        
        // 基于正文前100字生成描述
        String content = req.getContent();
        String description = content.length() > 150 
            ? content.substring(0, 150) + "..." 
            : content;
        response.setDescription(description);
        
        log.warn("使用兜底方案生成SEO内容");
        return response;
    }
}
