package com.github.paicoding.forum.service.chatai.service.impl.zhipu;

import cn.hutool.http.ContentType;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 智谱 Coding Plan OpenAI 兼容接入
 *
 * @author Codex
 * @date 2026/3/23
 */
@Slf4j
@Component
public class ZhipuCodingIntegration {
    private static final String DEFAULT_API_HOST = "https://open.bigmodel.cn/api/coding/paas/v4";
    private static final String DEFAULT_MODEL = "GLM-4.5-air";

    @Autowired
    private ZhipuCodingConfig config;
    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;

    private OkHttpClient okHttpClient;

    @PostConstruct
    public void init() {
        dynamicConfigContainer.registerRefreshCallback(config, this::refreshClient);
        refreshClient();
    }

    private void refreshClient() {
        long timeout = config.getTimeout() == null ? 900L : config.getTimeout();
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public boolean directReturn(ChatItemVo item) {
        ensureConfigured();
        ChatReq req = new ChatReq();
        req.setModel(resolveModel());
        req.setMessages(toMsg(item));
        req.setStream(false);

        try {
            Request request = new Request.Builder()
                    .url(resolveApiHost() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JsonUtil.toStr(req)))
                    .build();

            try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
                String responseBody = response.body() == null ? null : response.body().string();
                if (response.isSuccessful() && StringUtils.isNotBlank(responseBody)) {
                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(responseBody);
                    com.alibaba.fastjson.JSONArray choices = jsonObject.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        com.alibaba.fastjson.JSONObject firstChoice = choices.getJSONObject(0);
                        com.alibaba.fastjson.JSONObject message = firstChoice.getJSONObject("message");
                        String content = message == null ? null : message.getString("content");
                        if (StringUtils.isBlank(content)) {
                            throw new IllegalStateException("智谱 Coding 未返回 message.content");
                        }
                        item.initAnswer(content);
                        return true;
                    }
                }

                String errorMsg = buildErrorMessage(response.code(), responseBody);
                item.initAnswer(errorMsg);
                log.error("智谱 Coding 调用失败, code={}, body={}", response.code(), responseBody);
                return false;
            }
        } catch (Exception e) {
            log.error("智谱 Coding 调用失败", e);
            item.initAnswer(StringUtils.defaultIfBlank(e.getMessage(), "智谱 Coding 调用失败"));
            return false;
        }
    }

    public void streamReturn(ChatItemVo item, EventSourceListener listener) {
        executeStreamChat(toMsg(item), listener);
    }

    public void streamReturn(List<ChatItemVo> list, EventSourceListener listener) {
        List<ChatMsg> msgList = ChatConstants.toMsgList(list, this::toMsg);
        executeStreamChat(msgList, listener);
    }

    private void executeStreamChat(List<ChatMsg> messages, EventSourceListener listener) {
        ensureConfigured();
        ChatReq req = new ChatReq();
        req.setModel(resolveModel());
        req.setMessages(messages);
        req.setStream(true);

        try {
            EventSource.Factory factory = EventSources.createFactory(okHttpClient);
            Request request = new Request.Builder()
                    .url(resolveApiHost() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JsonUtil.toStr(req)))
                    .build();
            factory.newEventSource(request, listener);
        } catch (Exception e) {
            log.error("智谱 Coding 流式请求失败", e);
        }
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

    private String resolveModel() {
        return StringUtils.defaultIfBlank(config.getModel(), DEFAULT_MODEL);
    }

    private String buildErrorMessage(int code, String responseBody) {
        if (StringUtils.isBlank(responseBody)) {
            return "智谱 Coding 调用失败，HTTP " + code;
        }

        try {
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(responseBody);
            String msg = jsonObject.getString("message");
            if (StringUtils.isBlank(msg)) {
                msg = jsonObject.getString("error");
            }
            if (StringUtils.isBlank(msg) && jsonObject.getJSONObject("error") != null) {
                msg = jsonObject.getJSONObject("error").getString("message");
            }
            if (StringUtils.isNotBlank(msg)) {
                return "智谱 Coding 调用失败，HTTP " + code + "：" + msg;
            }
        } catch (Exception ignore) {
            // ignore
        }
        return "智谱 Coding 调用失败，HTTP " + code + "：" + responseBody;
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "zhipu.coding")
    public static class ZhipuCodingConfig {
        private String apiKey;
        private String apiHost;
        private String model;
        private Long timeout;
    }

    @Data
    public static class ChatReq {
        private String model;
        private boolean stream;
        private List<ChatMsg> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMsg {
        private String role;
        private String content;
    }

    private List<ChatMsg> toMsg(ChatItemVo item) {
        List<ChatMsg> list = new ArrayList<ChatMsg>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            list.add(new ChatMsg("system", item.getQuestion().substring(ChatConstants.PROMPT_TAG.length())));
            return list;
        }
        list.add(new ChatMsg("user", item.getQuestion()));
        if (StringUtils.isNotBlank(item.getAnswer())) {
            list.add(new ChatMsg("assistant", item.getAnswer()));
        }
        return list;
    }
}
