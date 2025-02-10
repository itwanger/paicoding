package com.github.paicoding.forum.service.chatai.service.impl.doubao;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.volcengine.ApiException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
        log.debug("豆包初始化 APIKEY:"+doubaoIntegration.getApiKey());
        String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        if (!StringUtils.hasText(doubaoIntegration.getApiKey())) {
            log.info("豆包API KEY 未配置，停止初始化DoubaoAiServiceImpl");
            this.service = null;
            return;
        }
        if(StringUtils.hasText(doubaoIntegration.getApiHost()) ){
            baseUrl = this.doubaoIntegration.getApiHost();
        }else {
            log.warn("豆包API HOST 未配置，使用默认值");
        }
        this.service = ArkService.builder()
                .baseUrl(baseUrl)
                .apiKey(this.doubaoIntegration.getApiKey())
                .build();
    }

    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        if (service == null) {
            log.warn("豆包ai服务未初始化成功 目前apikey:{}，目前apiHost:{}",doubaoIntegration.getApiKey(),doubaoIntegration.getApiHost());
            chat.initAnswer("Service not initialized");
            return AiChatStatEnum.ERROR;
        }
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
        if (service == null) {
            log.warn("豆包ai服务未初始化成功 目前apikey:{}，目前apiHost:{}",doubaoIntegration.getApiKey(),doubaoIntegration.getApiHost());
            ChatItemVo item = chatRes.getRecords().get(0);
            item.appendAnswer("Service not initialized").setAnswerType(ChatAnswerTypeEnum.STREAM_END);
            consumer.accept(AiChatStatEnum.ERROR, chatRes);
            return AiChatStatEnum.ERROR;
        }
        ChatItemVo item = chatRes.getRecords().get(0);
        List<ChatMessage> messages = ChatConstants.toMsgList(chatRes.getRecords(), this::toMsg);


        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("ep-20250208191823-mpjm8")
                .messages(messages)
                .build();
        // 异步返回
        Disposable disposable = service.streamChatCompletion(request)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())  // 如果不耗时 可以更换成Schedulers.single() 减少切换上下文的开销
                .doFinally(() -> {
                    // 流结束的逻辑
                    if(item.getAnswerType() != ChatAnswerTypeEnum.STREAM_END) {
                        // 检查下是不是已经结束了。
                        item.appendAnswer("\n").setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                        consumer.accept(AiChatStatEnum.END, chatRes);
                    }
                })
                .subscribe(choice -> {
                    if (!choice.getChoices().isEmpty()) {
                        item.appendAnswer((String) choice.getChoices().get(0).getMessage().getContent());
                        consumer.accept(AiChatStatEnum.MID, chatRes);
                    }
                }, throwable -> {
                            String errorMessage = "Error: " + throwable.getMessage();
                            if (throwable instanceof ApiException) {
                                ApiException apiException = (ApiException) throwable;
                                errorMessage = String.format("Error: %s, Code: %s, Param: %s",
                                        apiException.getMessage(), apiException.getCode(), apiException.getCause());
                            }
                            item.appendAnswer(errorMessage).setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                            consumer.accept(AiChatStatEnum.ERROR, chatRes);
                        }
                );


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


    private List<ChatMessage> toMsg(ChatItemVo item) {
        List<ChatMessage> list = new ArrayList<>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            // Prompt
            list.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(item.getQuestion().substring(ChatConstants.PROMPT_TAG.length())).build());
        } else {
            // 用户提问和回答
            list.add(ChatMessage.builder().role(ChatMessageRole.USER).content(item.getQuestion()).build());
            if (StringUtils.hasText(item.getAnswer())) {
                list.add(ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(item.getAnswer()).build());
            }
        }
        return list;
    }
}