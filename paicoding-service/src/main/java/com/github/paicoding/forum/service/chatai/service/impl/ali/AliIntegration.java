package com.github.paicoding.forum.service.chatai.service.impl.ali;

import com.alibaba.dashscope.common.ResultCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.deserialize.MessageDeserializeFactory;
import com.zhipu.oapi.service.v4.model.*;

import java.util.concurrent.Semaphore;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Slf4j
@Setter
@Component
public class AliIntegration {
    @Autowired
    private AliConfig config;

    public void streamReturn(Long user, ChatRecordsVo chatRecord, BiConsumer<AiChatStatEnum, ChatRecordsVo> callback) {
        try {
            ChatItemVo item = chatRecord.getRecords().get(0);

            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(item.getQuestion()).build();

            // 这里可以把历史信息也传进去，需要从数据库中获取，最好是从缓存中获取会比较好，还要考虑不能太多。

            GenerationParam param = GenerationParam.builder()
                    .model(config.getModel())
                    .messages(Arrays.asList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true)
                    .build();
            Semaphore semaphore = new Semaphore(0);
            StringBuilder fullContent = new StringBuilder();

            gen.streamCall(param, new ResultCallback<GenerationResult>() {
                @Override
                public void onEvent(GenerationResult message) {
                    String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                    fullContent.append(content);
                    log.info("Received message: {}", JsonUtils.toJson(message));
                    item.appendAnswer(content);
                    callback.accept(AiChatStatEnum.MID, chatRecord);
                }

                @Override
                public void onError(Exception err) {
                    callback.accept(AiChatStatEnum.ERROR, chatRecord);
                    log.error("Exception occurred: {}", err.getMessage());
                    semaphore.release();
                }

                @Override
                public void onComplete() {
                    item.setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                    callback.accept(AiChatStatEnum.END, chatRecord);
                    log.info("Completed");
                    semaphore.release();
                }
            });

            semaphore.acquire();
            log.info("Full content: \n{}", fullContent.toString());
        } catch (ApiException | NoApiKeyException | InputRequiredException | InterruptedException e) {
            log.error("An exception occurred: {}", e.getMessage());
        }
    }

    @Component
    @ConfigurationProperties(prefix = "ali")
    @Data
    public static class AliConfig {
        public String model;
    }

    public boolean directReturn(Long user, ChatItemVo chat) {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(chat.getQuestion())
                .build();
        GenerationParam param = GenerationParam.builder()
                .model(config.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        try {
            GenerationResult invokeModelApiResp = gen.call(param);

            chat.initAnswer(JsonUtil.toStr(invokeModelApiResp), ChatAnswerTypeEnum.JSON);
            log.info("阿里 AI 试用! 传参:{}, 返回:{}", chat, invokeModelApiResp);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }
}
