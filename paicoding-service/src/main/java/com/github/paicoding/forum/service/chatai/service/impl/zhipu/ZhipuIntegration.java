package com.github.paicoding.forum.service.chatai.service.impl.zhipu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.deserialize.MessageDeserializeFactory;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageAccumulator;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ChatTool;
import com.zhipu.oapi.service.v4.model.Choice;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zhipu.oapi.service.v4.model.ModelData;
import com.zhipu.oapi.service.v4.model.WebSearch;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Slf4j
@Setter
@Component
public class ZhipuIntegration {
    @Autowired
    private ZhipuConfig config;

    public void streamReturn(Long user, ChatRecordsVo chatRecord, BiConsumer<AiChatStatEnum, ChatRecordsVo> callback) {
        List<ChatMessage> messages = ChatConstants.toMsgList(chatRecord.getRecords(), this::toMsg);

        ChatItemVo item = chatRecord.getRecords().get(0);
        String requestId = String.format(config.requestIdTemplate, System.currentTimeMillis());
        // 函数调用参数构建部分
        List<ChatTool> chatToolList = new ArrayList<>();
        ChatTool chatTool = new ChatTool();

        chatTool.setType("web_search");
//        Retrieval retrieval = new Retrieval();
//        retrieval.setKnowledge_id("1826571496106102784");
        WebSearch webSearch = new WebSearch();
        webSearch.setEnable(Boolean.TRUE);
        chatTool.setWeb_search(webSearch);
//        chatTool.setType("code_interpreter");
        chatToolList.add(chatTool);

        // 请求参数封装
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(config.getModel())
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .tools(chatToolList)
                .userId("paicoding-" + String.valueOf(user))
                .toolChoice("auto")
                .requestId(requestId)
                .build();
        ClientV4 client = new ClientV4.Builder(config.apiSecretKey)
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();

        // 调用模型接口
        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);

        // 序列化输出
        ObjectMapper mapper = MessageDeserializeFactory.defaultObjectMapper();
        // 忽略未知字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 处理返回结果
        if (sseModelApiResp.isSuccess()) {
            AtomicBoolean isFirst = new AtomicBoolean(true);
            List<Choice> choices = new ArrayList<>();
            AtomicReference<ChatMessageAccumulator> lastAccumulator = new AtomicReference<>();

            mapStreamToAccumulator(sseModelApiResp.getFlowable()).doOnNext(accumulator -> {
                        {
                            if (isFirst.getAndSet(false)) {
                                log.info("智谱大模型开始返回结果 -> ");
                            }
                            if (accumulator.getDelta() != null && accumulator.getDelta().getTool_calls() != null) {
                                accumulator.getDelta().getTool_calls().forEach(toolCall -> {
                                    log.info("tool_call: {}", toolCall);
                                    JsonNode codeInterpreter = toolCall.get("code_interpreter");
                                    if (codeInterpreter != null) {
                                        // 检查并处理 outputs 字段
                                        JsonNode outputs = codeInterpreter.get("outputs");
                                        if (outputs != null) {
                                            outputs.forEach(output -> {
                                                log.info("output: {}", output);
                                                if (output.has("type") && output.get("type").asText().equals("file")) {
                                                    log.info("output file: {}", output.get("file"));
                                                    // 组装成 Markdown 返回
                                                    String content = "![file](" + output.get("file").asText() + ")";
                                                    item.appendAnswer(content);
                                                    callback.accept(AiChatStatEnum.MID, chatRecord);
                                                }
                                            });
                                        }
                                    }
                                });
                                String jsonString = mapper.writeValueAsString(accumulator.getDelta().getTool_calls());
                                if (log.isDebugEnabled()) {
                                    log.info("tool_calls: {}", jsonString);
                                }
                            }
                            if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                String content = accumulator.getDelta().getContent();
                                item.appendAnswer(content);
                                callback.accept(AiChatStatEnum.MID, chatRecord);
                                log.info("回复内容: {}", content);
                            }
                            choices.add(accumulator.getChoice());
                            lastAccumulator.set(accumulator);
                        }
                    })
                    .doOnComplete(() -> {
                        log.info("Stream completed.");
                        item.setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                        callback.accept(AiChatStatEnum.END, chatRecord);
                    })
                    .doOnError(throwable -> {
                        log.error("Error:", throwable);
                            callback.accept(AiChatStatEnum.ERROR, chatRecord);
                    }) // Handle errors
                    .blockingSubscribe();// Use blockingSubscribe instead of blockingGet()

            ChatMessageAccumulator chatMessageAccumulator = lastAccumulator.get();
            ModelData data = new ModelData();
            data.setChoices(choices);
            if (chatMessageAccumulator != null) {
                data.setUsage(chatMessageAccumulator.getUsage());
                data.setId(chatMessageAccumulator.getId());
                data.setCreated(chatMessageAccumulator.getCreated());
            }
            data.setRequestId(chatCompletionRequest.getRequestId());
            sseModelApiResp.setFlowable(null);// 打印前置空
            sseModelApiResp.setData(data);
        }
        try {
            log.info("model output: {}", mapper.writeValueAsString(sseModelApiResp));
        } catch (JsonProcessingException e) {
            log.error("An exception occurred: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        client.getConfig().getHttpClient().dispatcher().executorService().shutdown();

        client.getConfig().getHttpClient().connectionPool().evictAll();
        // List all active threads
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            log.info("Thread: " + t.getName() + " State: " + t.getState());
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
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), chat.getQuestion());
        messages.add(chatMessage);
        String requestId = String.format(config.requestIdTemplate, user + System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ClientV4 client = new ClientV4.Builder(config.apiSecretKey)
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        if (invokeModelApiResp.isSuccess()) {
            invokeModelApiResp.getData().getChoices().forEach(choice -> {
                chat.initAnswer(JsonUtil.toStr(choice.getMessage().getContent()), ChatAnswerTypeEnum.JSON);
                log.info("智谱 AI 试用! 传参:{}, 返回:{}", chat, invokeModelApiResp);
            });
        }


        return true;
    }

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    private List<ChatMessage> toMsg(ChatItemVo item) {
        List<ChatMessage> list = new ArrayList<>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            // 提示词消息
            list.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), item.getQuestion().substring(ChatConstants.PROMPT_TAG.length())));
            return list;
        }

        // 用户问答
        list.add(new ChatMessage(ChatMessageRole.USER.value(), item.getQuestion()));
        if (StringUtils.isNotBlank(item.getAnswer())) {
            list.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), item.getAnswer()));
        }
        return list;
    }
}
