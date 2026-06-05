package com.github.paicoding.forum.service.chatai.service.impl.zhipu;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.core.Constants;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ChatTool;
import ai.z.openapi.service.model.ChatToolType;
import ai.z.openapi.service.model.Choice;
import ai.z.openapi.service.model.Delta;
import ai.z.openapi.service.model.ModelData;
import ai.z.openapi.service.model.ToolCalls;
import ai.z.openapi.service.model.WebSearch;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Slf4j
@Setter
@Component
public class ZhipuIntegration {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Autowired
    private ZhipuConfig config;

    @Value("${ai.web-search.search-engine:search_pro}")
    private String webSearchEngine;

    @Value("${ai.web-search.count:5}")
    private int webSearchCount;

    @Value("${ai.web-search.search-recency-filter:noLimit}")
    private String webSearchRecencyFilter;

    @Value("${ai.web-search.content-size:high}")
    private String webSearchContentSize;

    public void streamReturn(Long user, ChatRecordsVo chatRecord, BiConsumer<AiChatStatEnum, ChatRecordsVo> callback) {
        List<ChatMessage> messages = ChatConstants.toMsgList(chatRecord.getRecords(), this::toMsg);
        ChatItemVo item = chatRecord.getRecords().get(0);
        String requestId = String.format(StringUtils.defaultIfBlank(config.requestIdTemplate, "paicoding-%d"), System.currentTimeMillis());

        ZhipuAiClient client = buildClient();
        try {
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(resolveModel())
                    .stream(Boolean.TRUE)
                    .messages(messages)
                    .tools(buildWebSearchTools())
                    .toolChoice("auto")
                    .requestId(requestId)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (!response.isSuccess() || response.getFlowable() == null) {
                item.initAnswer(buildErrorMessage(response), ChatAnswerTypeEnum.STREAM_END);
                callback.accept(AiChatStatEnum.ERROR, chatRecord);
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
                            log.info("智谱 AI tool_calls: {}", JsonUtil.toStr(delta.getTool_calls()));
                        }
                        String content = delta.getContent();
                        if (StringUtils.isNotBlank(content)) {
                            item.appendAnswer(content);
                            callback.accept(AiChatStatEnum.MID, chatRecord);
                        }
                    })
                    .doOnComplete(() -> {
                        item.setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                        callback.accept(AiChatStatEnum.END, chatRecord);
                    })
                    .doOnError(throwable -> {
                        item.initAnswer(StringUtils.defaultIfBlank(throwable.getMessage(), "智谱 AI 流式调用失败"), ChatAnswerTypeEnum.STREAM_END);
                        callback.accept(AiChatStatEnum.ERROR, chatRecord);
                    })
                    .blockingSubscribe();
        } finally {
            client.close();
        }
    }

    @Component
    @ConfigurationProperties(prefix = "zhipu")
    @Data
    public static class ZhipuConfig {
        public String requestIdTemplate;
        public String apiSecretKey;
        public String model;
    }

    public boolean directReturn(Long user, ChatItemVo chat) {
        return directReturn(user, java.util.Arrays.asList(chat), chat);
    }

    public boolean directReturn(Long user, List<ChatItemVo> chatList, ChatItemVo answerTarget) {
        if (StringUtils.isBlank(config.getApiSecretKey())) {
            throw new IllegalStateException("未配置 zhipu.apiSecretKey");
        }

        ZhipuAiClient client = buildClient();
        try {
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(resolveModel())
                    .stream(Boolean.FALSE)
                    .messages(ChatConstants.toMsgList(chatList, this::toMsg))
                    .tools(buildWebSearchTools())
                    .toolChoice("auto")
                    .requestId(String.format(StringUtils.defaultIfBlank(config.getRequestIdTemplate(), "paicoding-%d"), user + System.currentTimeMillis()))
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (!response.isSuccess()) {
                throw new IllegalStateException(buildErrorMessage(response));
            }
            String answer = extractAnswer(response.getData());
            if (StringUtils.isBlank(answer)) {
                throw new IllegalStateException("智谱 AI 返回结构缺少 message.content");
            }
            answerTarget.initAnswer(answer, ChatAnswerTypeEnum.JSON);
            log.info("智谱 AI 试用! 传参:{}, 返回:{}", chatList, response);
            return true;
        } finally {
            client.close();
        }
    }

    private ZhipuAiClient buildClient() {
        if (StringUtils.isBlank(config.getApiSecretKey())) {
            throw new IllegalStateException("未配置 zhipu.apiSecretKey");
        }
        return ZhipuAiClient.builder()
                .ofZHIPU()
                .apiKey(config.getApiSecretKey())
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(8, 1, TimeUnit.SECONDS)
                .build();
    }

    private String resolveModel() {
        return StringUtils.defaultIfBlank(config.getModel(), Constants.ModelChatGLM4);
    }

    private List<ChatTool> buildWebSearchTools() {
        List<ChatTool> tools = new ArrayList<>();
        tools.add(ChatTool.builder()
                .type(ChatToolType.WEB_SEARCH.value())
                .webSearch(WebSearch.builder()
                        .enable(Boolean.TRUE)
                        .searchResult(Boolean.TRUE)
                        .requireSearch(Boolean.FALSE)
                        .searchEngine(StringUtils.defaultIfBlank(webSearchEngine, "search_pro"))
                        .count(Math.min(50, Math.max(1, webSearchCount)))
                        .searchRecencyFilter(StringUtils.defaultIfBlank(webSearchRecencyFilter, "noLimit"))
                        .contentSize(StringUtils.defaultIfBlank(webSearchContentSize, "high"))
                        .searchPrompt("今天是" + LocalDate.now().format(DATE_FORMATTER)
                                + "。如果用户问题涉及现在、最新、截至、价格、版本、发布时间、数量、行业现状、市场采用情况等时效信息，"
                                + "请优先结合网络搜索结果回答；如果搜索结果与既有知识或参考资料冲突，以更新信息为准，并简要说明时间差异。")
                        .build())
                .build());
        return tools;
    }

    private List<ChatMessage> toMsg(ChatItemVo item) {
        List<ChatMessage> list = new ArrayList<>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            list.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM.value())
                    .content(item.getQuestion().substring(ChatConstants.PROMPT_TAG.length()))
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
            return "智谱 AI 未返回结果";
        }
        if (response.getError() != null && StringUtils.isNotBlank(response.getError().getMessage())) {
            return "智谱 AI 调用失败: " + response.getError().getMessage();
        }
        return "智谱 AI 调用失败: " + StringUtils.defaultIfBlank(response.getMsg(), "code=" + response.getCode());
    }
}
