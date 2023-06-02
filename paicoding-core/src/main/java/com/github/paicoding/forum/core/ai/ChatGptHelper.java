package com.github.paicoding.forum.core.ai;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.plexpt.chatgpt.ChatGPT;
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
public class ChatGptHelper {

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

    public boolean simpleGptReturn(Long routingKey, ChatRecord record) {
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
