package com.github.paicoding.forum.service.chatai.service.impl.chatgpt;

import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.net.ProxyCenter;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.billing.CreditGrantsResponse;
import com.plexpt.chatgpt.entity.chat.ChatChoice;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author YiHui
 * @date 2023/4/20
 */
@Slf4j
@Service
public class ChatGptIntegration {

    @Value("${chatgpt.key}")
    private String key;


    /**
     * 每个用户的会话缓存
     */
    public LoadingCache<Long, ChatGPT> cacheStream;

    @PostConstruct
    public void initKey() {
        cacheStream = CacheBuilder.newBuilder()
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .build(new CacheLoader<Long, ChatGPT>() {
                    @Override
                    public ChatGPT load(Long s) throws Exception {
                        return simpleGPT(s);
                    }
                });

        log.info("当前选中key:{}", key);
    }

    public String setKey(String key) {
        this.key = key;
        return this.key;
    }


    /**
     * 基于routingkey进行路由
     *
     * @param routingKey
     * @return
     */
    private ChatGPT simpleGPT(Long routingKey) {
        return ChatGPT.builder()
                .apiKey(key)
                .proxy(ProxyCenter.loadProxy(String.valueOf(routingKey)))
                .timeout(900)
                .apiHost("https://api.openai.com/")
                .build()
                .init();
    }

    public ChatGPT getGpt(Long routingKey) {
        return cacheStream.getUnchecked(routingKey);
    }

    /**
     * 账户信息
     *
     * @return
     */
    public String creditInfo() {
        CreditGrantsResponse response = getGpt(0L).creditGrants();
        return JsonUtil.toStr(response);
    }

    public boolean gptReturn(Long routingKey, ChatItemVo chat) {
        ChatGPT gpt = getGpt(routingKey);
        try {
            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                    .messages(Arrays.asList(Message.of(chat.getQuestion())))
                    .maxTokens(3000)
                    .temperature(0.9)
                    .build();
            ChatCompletionResponse response = gpt.chatCompletion(chatCompletion);
            List<ChatChoice> list = response.getChoices();
            chat.initAnswer(JsonUtil.toStr(list));
            log.info("chatgpt试用! 传参:{}, 返回:{}", chat, list);
            return false;
        } catch (Exception e) {
            // 对于系统异常，不用继续等待了
            chat.initAnswer(e.getMessage());
            log.info("chatgpt执行异常！ key:{}", chat, e);
            return false;
        }
    }

    /**
     * 一个基础的chatgpt问答
     *
     * @param routingKey
     * @param record
     * @return
     */
    public boolean gptReturn(Long routingKey, ChatRecord record) {
        ChatGPT gpt = getGpt(routingKey);
        try {
            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                    .messages(Arrays.asList(Message.of(record.getQas())))
                    .maxTokens(3000)
                    .temperature(0.9)
                    .build();
            ChatCompletionResponse response = gpt.chatCompletion(chatCompletion);
            List<ChatChoice> list = response.getChoices();
            log.info("chatgpt试用! 传参:{}, 返回:{}", record.getQas(), list);
            record.setRes(list);
            return true;
        } catch (Exception e) {
            // 对于系统异常，不用继续等待了
            record.setSysErr(e.getMessage());
            log.info("chatgpt执行异常！ key:{}", record.getQas(), e);
            return false;
        }
    }
}
