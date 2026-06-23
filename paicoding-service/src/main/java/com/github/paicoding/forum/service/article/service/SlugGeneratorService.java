package com.github.paicoding.forum.service.article.service;

import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuCodingIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * URL Slug AI生成服务
 * 使用大模型提炼更语义化的短链接
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
    private static final long SLUG_AI_TIMEOUT_SECONDS = 15L;

    @Autowired(required = false)
    private ZhipuCodingIntegration zhipuCodingIntegration;
    @Autowired(required = false)
    private ZhipuCodingIntegration.ZhipuCodingConfig zhipuCodingConfig;

    @Autowired(required = false)
    private DeepSeekIntegration deepSeekIntegration;
    @Autowired(required = false)
    private DeepSeekIntegration.DeepSeekConf deepSeekConf;

    @Autowired(required = false)
    private ZhipuIntegration zhipuIntegration;

    @Autowired(required = false)
    private AiConfig aiConfig;

    /**
     * 使用AI生成URL slug
     *
     * @param title 文章标题
     * @return URL友好的slug
     */
    public String generateSlugWithAI(String title) {
        return generateSlugWithAI(title, null);
    }

    /**
     * 使用AI生成URL slug
     *
     * @param title         文章标题
     * @param columnUrlSlug 所属专栏URL标识
     * @return URL友好的slug
     */
    public String generateSlugWithAI(String title, String columnUrlSlug) {
        if (StringUtils.isBlank(title)) {
            return "";
        }

        String prompt = buildPrompt(title, columnUrlSlug);
        AISourceEnum source = resolveSlugSource();
        return generateWithSource(source, prompt, title);
    }

    private AISourceEnum resolveSlugSource() {
        if (aiConfig == null || aiConfig.getSource() == null || aiConfig.getSource().isEmpty()) {
            return AISourceEnum.ZHIPU_CODING;
        }

        List<AISourceEnum> configured = aiConfig.getSource();
        for (AISourceEnum source : configured) {
            if (isSupportedSlugSource(source)) {
                return source;
            }
        }
        return AISourceEnum.ZHIPU_CODING;
    }

    private boolean isSupportedSlugSource(AISourceEnum source) {
        return source == AISourceEnum.ZHIPU_CODING || source == AISourceEnum.DEEP_SEEK || source == AISourceEnum.ZHI_PU_AI;
    }

    private String generateWithSource(AISourceEnum source, String prompt, String title) {
        try {
            String answer;
            switch (source) {
                case ZHIPU_CODING:
                    answer = callZhipuCoding(prompt);
                    break;
                case DEEP_SEEK:
                    answer = callDeepSeek(prompt);
                    break;
                case ZHI_PU_AI:
                    answer = callZhipu(prompt);
                    break;
                default:
                    throw new IllegalStateException("URL slug 生成暂不支持该模型: " + source);
            }

            String slug = cleanAIResponse(answer);
            if (StringUtils.isNotBlank(slug) && isValidSlug(slug)) {
                log.info("Generated slug with {} for title '{}': {}", source, title, slug);
                return slug;
            }
            throw new IllegalStateException(source + " 返回的 slug 无效: " + answer);
        } catch (RuntimeException e) {
            log.warn("AI slug generation failed, source={}, title='{}', reason={}", source, title, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.warn("AI slug generation failed, source={}, title='{}', reason={}", source, title, e.getMessage(), e);
            throw new IllegalStateException(source + " 生成语义 URL 失败: " + e.getMessage(), e);
        }
    }

    private String callZhipuCoding(String prompt) throws IOException {
        if (zhipuCodingConfig == null || StringUtils.isBlank(zhipuCodingConfig.getApiKey())) {
            throw new IllegalStateException("未配置 zhipu.coding.apiKey");
        }

        OkHttpClient client = buildClient();
        ZhipuCodingReq req = new ZhipuCodingReq();
        req.setModel(StringUtils.defaultIfBlank(zhipuCodingConfig.getModel(), "GLM-4.5-air"));
        req.setStream(false);
        req.setMessages(Collections.singletonList(new ChatMsg("user", prompt)));
        req.setThinking(new Thinking(StringUtils.defaultIfBlank(zhipuCodingConfig.getThinkingType(), "disabled")));
        req.setMaxTokens(zhipuCodingConfig.getMaxTokens() == null || zhipuCodingConfig.getMaxTokens() <= 0 ? 256 : zhipuCodingConfig.getMaxTokens());
        req.setTemperature(zhipuCodingConfig.getTemperature() == null ? 0.3D : zhipuCodingConfig.getTemperature());

        String requestUrl = StringUtils.defaultIfBlank(zhipuCodingConfig.getApiHost(), "https://open.bigmodel.cn/api/coding/paas/v4") + "/chat/completions";
        String requestJson = JsonUtil.toStr(req);
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", "Bearer " + zhipuCodingConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestJson))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? null : response.body().string();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("智谱 Coding 调用失败，HTTP " + response.code());
            }
            return parseContent(responseBody, "智谱 Coding");
        }
    }

    private String callDeepSeek(String prompt) throws IOException {
        if (deepSeekConf == null || StringUtils.isBlank(deepSeekConf.getApiKey())) {
            throw new IllegalStateException("未配置 deepseek.apiKey");
        }

        OkHttpClient client = buildClient();
        JSONObject req = new JSONObject();
        req.put("model", StringUtils.defaultIfBlank(deepSeekConf.getModel(), "deepseek-chat"));
        req.put("stream", false);
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        req.put("messages", messages);

        Request request = new Request.Builder()
                .url(StringUtils.defaultIfBlank(deepSeekConf.getApiHost(), "https://api.deepseek.com") + "/chat/completions")
                .addHeader("Authorization", "Bearer " + deepSeekConf.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), req.toJSONString()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? null : response.body().string();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("DeepSeek 调用失败，HTTP " + response.code());
            }
            return parseContent(responseBody, "DeepSeek");
        }
    }

    private String callZhipu(String prompt) {
        if (zhipuIntegration == null) {
            throw new IllegalStateException("未配置智谱 AI 集成");
        }
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!zhipuIntegration.directReturn(0L, item) || StringUtils.isBlank(item.getAnswer())) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "智谱 AI 调用失败"));
        }
        return item.getAnswer();
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(SLUG_AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(SLUG_AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(SLUG_AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(SLUG_AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    private String parseContent(String responseBody, String source) {
        if (StringUtils.isBlank(responseBody)) {
            throw new IllegalStateException(source + " 返回为空");
        }

        JSONObject jsonObject = JSON.parseObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException(source + " 未返回 choices");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null || StringUtils.isBlank(message.getString("content"))) {
            throw new IllegalStateException(source + " 未返回 message.content");
        }
        return message.getString("content");
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(String title) {
        return buildPrompt(title, null);
    }

    private String buildPrompt(String title, String columnUrlSlug) {
        String columnContext = StringUtils.isBlank(columnUrlSlug) ? "" :
                "专栏URL标识：" + columnUrlSlug.trim().toLowerCase() + "\n" +
                        "这是该专栏下的教程文章。生成的是文章URL Slug，不是专栏URL Slug。\n" +
                        "当前系统的文章短链会直接使用文章URL Slug，所以必须让文章URL Slug包含专栏/项目关键词，避免脱离专栏上下文。\n" +
                        "如果标题是“什么是/是什么”介绍类，使用 what-is-{专栏URL标识}。\n" +
                        "其他教程类标题，使用 {专栏URL标识}-{对象或主题}-{动作或章节}。\n\n";
        return "任务：根据文章标题和专栏上下文生成英文 URL Slug\n" +
                "要求：\n" +
                "- 输出小写英文、数字和连字符，不能包含中文、空格、解释或标点\n" +
                "- 优先保留专栏/项目关键词，例如 paismart、paiagent、paicoding\n" +
                "- 不要输出过于泛化的 slug，例如 resume-writing、rag-guide、introduction\n" +
                "- 不要把标题逐字拼音化，要翻译成 SEO 友好的英文短语\n" +
                "- 总长度控制在 2-4 个英文词内\n" +
                "- 只返回slug，无任何解释\n\n" +
                columnContext +
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
                "派聪明AI助手介绍 => paismart-intro\n" +
                "派聪明RAG项目是什么 => what-is-paismart\n" +
                "派聪明如何写简历 => paismart-resume-write\n" +
                "PaiAgent 工作流入门 => paiagent-workflow-start\n" +
                "如何搭建知识库 => paismart-knowledge-base-build\n" +
                "什么是PaiAgent => what-is-paiagent\n\n" +
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

        String cleaned = aiResponse.trim();
        String[] lines = cleaned.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.isBlank(line) || line.contains("：") || line.contains(":")
                    || line.contains("标题") || line.length() < 3) {
                continue;
            }
            line = line.replaceAll("[\"'`]", "");
            line = line.toLowerCase();
            line = line.replaceAll("[^a-z0-9-]", "");

            if (StringUtils.isNotBlank(line) && line.matches("^[a-z0-9-]+$")) {
                return limitLength(line);
            }
        }

        cleaned = cleaned.toLowerCase();
        cleaned = cleaned.replaceAll("[^a-z0-9-\\s]", "");
        cleaned = WHITESPACE.matcher(cleaned).replaceAll("-");
        cleaned = DUPLICATE_DASH.matcher(cleaned).replaceAll("-");
        cleaned = cleaned.replaceAll("^-+|-+$", "");

        return limitLength(cleaned);
    }

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

    private boolean isValidSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return false;
        }
        return slug.matches("^[a-z0-9-]+$") && slug.length() <= MAX_SLUG_LENGTH;
    }

    @Data
    private static class ZhipuCodingReq {
        private String model;
        private boolean stream;
        private List<ChatMsg> messages;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private Thinking thinking;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Thinking {
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatMsg {
        private String role;
        private String content;
    }
}
