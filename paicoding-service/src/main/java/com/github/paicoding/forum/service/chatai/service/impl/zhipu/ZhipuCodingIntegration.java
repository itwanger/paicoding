package com.github.paicoding.forum.service.chatai.service.impl.zhipu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ChatThinking;
import ai.z.openapi.service.model.ChatTool;
import ai.z.openapi.service.model.ChatToolType;
import ai.z.openapi.service.model.Choice;
import ai.z.openapi.service.model.Delta;
import ai.z.openapi.service.model.MCPTool;
import ai.z.openapi.service.model.McpToolTransportType;
import ai.z.openapi.service.model.ModelData;
import ai.z.openapi.service.model.ToolCalls;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 智谱 Coding Plan 接入。
 *
 * @author Codex
 * @date 2026/3/23
 */
@Slf4j
@Component
public class ZhipuCodingIntegration {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final String DEFAULT_API_HOST = "https://open.bigmodel.cn/api/coding/paas/v4";
    private static final String DEFAULT_MODEL = "GLM-4.5-air";
    private static final String DEFAULT_THINKING_TYPE = "disabled";
    private static final int DEFAULT_MAX_TOKENS = 512;
    private static final double DEFAULT_TEMPERATURE = 0.3D;
    private static final String DEFAULT_MCP_URL = "https://open.bigmodel.cn/api/mcp/web_search_prime/mcp";

    @Autowired
    private ZhipuCodingConfig config;
    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;

    @Value("${ai.web-search.coding-mcp-url:https://open.bigmodel.cn/api/mcp/web_search_prime/mcp}")
    private String codingMcpUrl;

    @PostConstruct
    public void init() {
        dynamicConfigContainer.registerRefreshCallback(config, () -> {
        });
    }

    public boolean directReturn(ChatItemVo item) {
        return directReturn(java.util.Arrays.asList(item), item);
    }

    public boolean directReturn(List<ChatItemVo> items, ChatItemVo answerTarget) {
        ZhipuAiClient client = null;
        try {
            client = buildClient();
            ChatCompletionCreateParams request = baseRequest(ChatConstants.toMsgList(items, this::toMsg), Boolean.FALSE)
                    .tools(buildMcpTools())
                    .toolChoice(isWebSearchToolEnabled() ? "auto" : null)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (!response.isSuccess()) {
                answerTarget.initAnswer(buildErrorMessage(response));
                return false;
            }
            String answer = extractAnswer(response.getData());
            if (StringUtils.isBlank(answer)) {
                answerTarget.initAnswer("智谱 Coding 未返回 message.content");
                return false;
            }
            answerTarget.initAnswer(answer);
            return true;
        } catch (Exception e) {
            log.error("智谱 Coding 调用失败", e);
            answerTarget.initAnswer(StringUtils.defaultIfBlank(e.getMessage(), "智谱 Coding 调用失败"));
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public boolean isWebSearchToolEnabled() {
        return StringUtils.isNotBlank(config.getApiKey());
    }

    public void streamReturn(ChatItemVo item, EventSourceListener listener) {
        streamReturn(java.util.Arrays.asList(item), listener);
    }

    public void streamReturn(List<ChatItemVo> list, EventSourceListener listener) {
        ZhipuAiClient client = null;
        try {
            client = buildClient();
            ChatCompletionCreateParams request = baseRequest(ChatConstants.toMsgList(list, this::toMsg), Boolean.TRUE)
                    .tools(buildMcpTools())
                    .toolChoice(isWebSearchToolEnabled() ? "auto" : null)
                    .build();
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (!response.isSuccess() || response.getFlowable() == null) {
                listener.onFailure(null, new IllegalStateException(buildErrorMessage(response)), null);
                return;
            }
            response.getFlowable().doOnNext(data -> {
                        if (data.getChoices() == null || data.getChoices().isEmpty()) {
                            return;
                        }
                        Delta delta = data.getChoices().get(0).getDelta();
                        if (delta == null) {
                            return;
                        }
                        if (delta.getTool_calls() != null) {
                            log.info("智谱 Coding tool_calls: {}", JsonUtil.toStr(delta.getTool_calls()));
                        }
                        if (StringUtils.isNotBlank(delta.getContent())) {
                            emitContent(listener, delta.getContent());
                        }
                    })
                    .doOnComplete(() -> listener.onEvent(null, null, null, "[DONE]"))
                    .doOnError(throwable -> listener.onFailure(null, throwable, null))
                    .blockingSubscribe();
        } catch (Exception e) {
            listener.onFailure(null, e, null);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void emitContent(EventSourceListener listener, String content) {
        JSONObject delta = new JSONObject();
        delta.put("content", content);
        JSONObject choice = new JSONObject();
        choice.put("delta", delta);
        JSONArray choices = new JSONArray();
        choices.add(choice);
        JSONObject event = new JSONObject();
        event.put("choices", choices);
        listener.onEvent(null, null, null, event.toJSONString());
    }

    private ChatCompletionCreateParams.ChatCompletionCreateParamsBuilder<?, ?> baseRequest(List<ChatMessage> messages, Boolean stream) {
        return ChatCompletionCreateParams.builder()
                .model(resolveModel())
                .messages(messages)
                .stream(stream)
                .thinking(ChatThinking.builder().type(resolveThinkingType()).build())
                .maxTokens(resolveMaxTokens())
                .temperature(resolveTemperature().floatValue());
    }

    private ZhipuAiClient buildClient() {
        ensureConfigured();
        return ZhipuAiClient.builder()
                .apiKey(config.getApiKey())
                .baseUrl(normalizeBaseUrl(resolveApiHost()))
                .networkConfig(resolveTimeout(), resolveTimeout(), resolveTimeout(), resolveTimeout(), TimeUnit.SECONDS)
                .connectionPool(8, 1, TimeUnit.SECONDS)
                .build();
    }

    private List<ChatTool> buildMcpTools() {
        if (!isWebSearchToolEnabled()) {
            return Collections.emptyList();
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + config.getApiKey());

        MCPTool mcpTool = MCPTool.builder()
                .server_label("web-search-prime")
                .server_url(StringUtils.defaultIfBlank(codingMcpUrl, DEFAULT_MCP_URL))
                .transport_type(McpToolTransportType.STREAMABLE_HTTP.getCode())
                .allowed_tools(Collections.singleton("webSearchPrime"))
                .headers(headers)
                .build();

        List<ChatTool> tools = new ArrayList<ChatTool>();
        tools.add(ChatTool.builder()
                .type(ChatToolType.MCP.value())
                .mcp(mcpTool)
                .build());
        return tools;
    }

    private List<ChatMessage> toMsg(ChatItemVo item) {
        List<ChatMessage> list = new ArrayList<ChatMessage>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            list.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM.value())
                    .content(item.getQuestion().substring(ChatConstants.PROMPT_TAG.length()) + "\n\n" + buildWebSearchPrompt())
                    .build());
            return list;
        }
        list.add(ChatMessage.builder()
                .role(ChatMessageRole.USER.value())
                .content(item.getQuestion())
                .build());
        if (StringUtils.isNotBlank(item.getAnswer())) {
            list.add(ChatMessage.builder()
                    .role(ChatMessageRole.ASSISTANT.value())
                    .content(item.getAnswer())
                    .build());
        }
        return list;
    }

    private String buildWebSearchPrompt() {
        if (!isWebSearchToolEnabled()) {
            return "";
        }
        return "当前日期：" + LocalDate.now().format(DATE_FORMATTER)
                + "。当用户问题涉及现在、最新、截至、价格、版本、发布时间、数量、行业现状、市场采用情况等时效信息，"
                + "或参考资料可能过期时，请使用 web-search-prime MCP 的 webSearchPrime 工具获取最新信息。最终回答不要暴露工具调用过程。";
    }

    private String extractAnswer(ModelData data) {
        if (data == null || data.getChoices() == null || data.getChoices().isEmpty()) {
            return null;
        }
        for (Choice choice : data.getChoices()) {
            if (choice == null || choice.getMessage() == null) {
                continue;
            }
            Object content = choice.getMessage().getContent();
            if (content instanceof String && StringUtils.isNotBlank((String) content)) {
                return (String) content;
            }
            if (content != null) {
                return JsonUtil.toStr(content);
            }
            List<ToolCalls> toolCalls = choice.getMessage().getToolCalls();
            if (toolCalls != null && !toolCalls.isEmpty()) {
                return JsonUtil.toStr(toolCalls);
            }
        }
        return null;
    }

    private String buildErrorMessage(ChatCompletionResponse response) {
        if (response == null) {
            return "智谱 Coding 未返回结果";
        }
        if (response.getError() != null && StringUtils.isNotBlank(response.getError().getMessage())) {
            return "智谱 Coding 调用失败: " + response.getError().getMessage();
        }
        return "智谱 Coding 调用失败: " + StringUtils.defaultIfBlank(response.getMsg(), "code=" + response.getCode());
    }

    private void ensureConfigured() {
        if (StringUtils.isBlank(config.getApiKey())) {
            throw new IllegalStateException("未配置 zhipu.coding.apiKey");
        }
        if (StringUtils.isBlank(resolveApiHost())) {
            throw new IllegalStateException("未配置 zhipu.coding.apiHost");
        }
        if (StringUtils.isBlank(resolveModel())) {
            throw new IllegalStateException("未配置 zhipu.coding.model");
        }
    }

    private String resolveApiHost() {
        return StringUtils.defaultIfBlank(config.getApiHost(), DEFAULT_API_HOST);
    }

    private String normalizeBaseUrl(String apiHost) {
        String baseUrl = StringUtils.defaultIfBlank(apiHost, DEFAULT_API_HOST);
        return StringUtils.endsWith(baseUrl, "/") ? baseUrl : baseUrl + "/";
    }

    private String resolveModel() {
        return StringUtils.defaultIfBlank(config.getModel(), DEFAULT_MODEL);
    }

    private String resolveThinkingType() {
        return StringUtils.defaultIfBlank(config.getThinkingType(), DEFAULT_THINKING_TYPE);
    }

    private Integer resolveMaxTokens() {
        return config.getMaxTokens() == null || config.getMaxTokens() <= 0 ? DEFAULT_MAX_TOKENS : config.getMaxTokens();
    }

    private Double resolveTemperature() {
        return config.getTemperature() == null ? DEFAULT_TEMPERATURE : config.getTemperature();
    }

    private int resolveTimeout() {
        Long timeout = config.getTimeout();
        return timeout == null || timeout <= 0 ? 900 : timeout.intValue();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "zhipu.coding")
    public static class ZhipuCodingConfig {
        private String apiKey;
        private String apiHost;
        private String model;
        private Long timeout;
        private Integer maxTokens;
        private Double temperature;
        private String thinkingType;
    }
}
