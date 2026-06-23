package com.github.paicoding.forum.service.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateRes;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.util.UrlSlugUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnDao;
import com.github.paicoding.forum.service.article.service.AiSeoService;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AI SEO生成服务实现
 *
 * @author 沉默王二
 * @date 2026/1/28
 */
@Slf4j
@Service
public class AiSeoServiceImpl implements AiSeoService {

    private static final long PROVIDER_TIMEOUT_SECONDS = 240L;
    
    @Autowired
    private ZhipuIntegration zhipuIntegration;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnDao columnDao;
    
    private static final String SEO_PROMPT_TEMPLATE = 
            "你是一个专业的SEO优化专家。根据以下信息生成SEO优化的标题、描述和URL slug：\n\n" +
            "短标题：%s\n" +
            "正文内容：%s\n\n" +
            "要求：\n" +
            "1. 标题(title)：基于短标题扩展，长度控制在50-60个字符，包含关键词，吸引点击\n" +
            "2. 描述(description)：总结文章核心内容，长度控制在150-160个字符，自然融入关键词\n" +
            "3. URL slug(urlSlug)：用英文小写单词和连字符表达文章核心主题，长度控制在3-8个词，只能包含小写字母、数字和连字符，不能是纯数字\n" +
            "4. 三者都要符合SEO最佳实践，提高搜索引擎排名\n\n" +
            "重要：请严格按照以下JSON格式返回，不要添加任何额外说明文字、代码块标记或换行符：\n" +
            "{\"title\":\"生成的SEO标题\",\"description\":\"生成的SEO描述\",\"urlSlug\":\"generated-url-slug\"}";
    
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
        
        log.info("尝试使用智谱AI生成SEO内容");
        ChatItemVo zhipuItem = new ChatItemVo().initQuestion(prompt);
        callProviderWithTimeout("智谱AI", () -> zhipuIntegration.directReturn(0L, zhipuItem));
        String answer = zhipuItem.getAnswer();
        if (StringUtils.isBlank(answer)) {
            throw aiSeoException("智谱AI返回内容为空");
        }
        
        try {
            AiSeoGenerateRes result = parseZhipuResponse(answer);
            
            // 验证结果
            if (StringUtils.isBlank(result.getTitle()) || StringUtils.isBlank(result.getDescription())) {
                throw aiSeoException("智谱AI返回的标题或描述为空");
            }
            if (StringUtils.isBlank(result.getUrlSlug())) {
                throw aiSeoException("智谱AI返回的URL slug为空");
            }

            result.setUrlSlug(resolveSeoSlug(result.getUrlSlug(), req.getArticleId()));
            
            log.info("AI生成SEO内容成功，短标题：{}，生成标题：{}", req.getShortTitle(), result.getTitle());
            return result;
            
        } catch (Exception e) {
            if (e instanceof ForumException) {
                throw e;
            }
            log.error("解析智谱AI响应失败", e);
            throw aiSeoException("解析智谱AI响应失败: " + e.getMessage());
        }
    }

    private void callProviderWithTimeout(String providerName, Callable<Boolean> callable) {
        boolean success;
        try {
            success = Boolean.TRUE.equals(AsyncUtil.callWithTimeLimit(PROVIDER_TIMEOUT_SECONDS, TimeUnit.SECONDS, callable));
        } catch (TimeoutException e) {
            log.warn("{}生成SEO内容超时，限制：{}秒", providerName, PROVIDER_TIMEOUT_SECONDS, e);
            throw aiSeoException(providerName + "调用超时，限制：" + PROVIDER_TIMEOUT_SECONDS + "秒");
        } catch (ForumException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}生成SEO内容异常", providerName, e);
            throw aiSeoException(providerName + "调用异常: " + e.getMessage());
        }
        if (!success) {
            throw aiSeoException(providerName + "调用失败");
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
        
        throw aiSeoException("智谱AI返回内容不是标准JSON格式");
    }

    private String resolveSeoSlug(String source, Long articleId) {
        String baseSlug = UrlSlugUtil.generateSlug(source);
        if (StringUtils.isBlank(baseSlug)) {
            throw aiSeoException("智谱AI返回的URL slug无效");
        }
        if (StringUtils.isNumeric(baseSlug)) {
            throw aiSeoException("智谱AI返回的URL slug不能是纯数字");
        }

        String slug = baseSlug;
        int suffix = 2;
        while (articleDao.existsUrlSlug(slug, articleId) || columnDao.existsUrlSlug(slug, null)) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private RuntimeException aiSeoException(String message) {
        return ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, message);
    }
}
