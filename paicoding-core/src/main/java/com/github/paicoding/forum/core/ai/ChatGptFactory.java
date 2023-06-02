package com.github.paicoding.forum.core.ai;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;


/**
 * @author YiHui
 * @date 2023/4/20
 */
@Slf4j
@Service
public class ChatGptFactory {
    @Value("${chatgpt.key}")
    private String key;

    @PostConstruct
    public void initKey() {
        log.info("当前选中key:{}", key);
    }

    public String setKey(String key) {
        this.key = key;
        return this.key;
    }

    /**
     * 每个用户的会话缓存
     */
    public LoadingCache<Long, ChatGPTStream> cacheStream = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.SECONDS)
            .build(new CacheLoader<Long, ChatGPTStream>() {
                @Override
                public ChatGPTStream load(Long s) throws Exception {
                    return createStream(s);
                }
            });


    private ChatGPTStream createStream(long userId) {
        return ChatGPTStream.builder()
                .timeout(300)
                .apiKey(key)
                .proxy(ProxyCenter.loadProxy(String.valueOf(userId)))
                .apiHost("https://api.openai.com/")
                .build()
                .init();
    }


    public ChatGPT simpleGPT() {
        return ChatGPT.builder()
                .apiKey(key)
                .proxy(ProxyCenter.loadProxy("chatgpt"))
                .timeout(900)
                .apiHost("https://api.openai.com/")
                .build()
                .init();
    }
}
