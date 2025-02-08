package com.github.paicoding.forum.service.chatai.service.impl.doubao;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
@Service

@DependsOn(value ="doubaoIntegration")
public class DoubaoAiServiceImpl extends AbsChatService {
    private final ArkService service;
    @Autowired
    private DoubaoIntegration doubaoIntegration;

    @Autowired
    public DoubaoAiServiceImpl(DoubaoIntegration doubaoIntegration) {
        this.doubaoIntegration = doubaoIntegration;
        log.info("豆包初始化 APIKEY:"+doubaoIntegration.getApiKey());
        this.service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .apiKey(this.doubaoIntegration.getApiKey())
                .build();
    }

    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build());
        messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(chat.getQuestion()).build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("ep-20250208191823-mpjm8")
                .messages(messages)
                .build();

        try {
            String response = (String) service.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
            chat.initAnswer(response, ChatAnswerTypeEnum.TEXT);
            return AiChatStatEnum.END;
        } catch (Exception e) {
            chat.initAnswer("Error: " + e.getMessage());
            return AiChatStatEnum.ERROR;
        }
    }

    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        ChatItemVo item = chatRes.getRecords().get(0);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build());
        messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(item.getQuestion()).build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("ep-20250208191823-mpjm8")
                .messages(messages)
                .build();

        service.streamChatCompletion(request)
                .doOnError(throwable -> {
                    item.appendAnswer("Error: " + throwable.getMessage()).setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                    consumer.accept(AiChatStatEnum.ERROR, chatRes);
                })
                .blockingForEach(choice -> {
                    if (choice.getChoices().size() > 0) {
                        item.appendAnswer((String) choice.getChoices().get(0).getMessage().getContent());
                        consumer.accept(AiChatStatEnum.MID, chatRes);
                    }
                });

        return AiChatStatEnum.IGNORE;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.DOU_BAO_AI;
    }

    @Override
    public boolean asyncFirst() {
        return true;
    }
}