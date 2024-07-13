package com.github.paicoding.forum.service.chatai.service.impl.chatgpt;

import cn.hutool.core.util.RandomUtil;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.net.ProxyCenter;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.billing.CreditGrantsResponse;
import com.plexpt.chatgpt.entity.chat.ChatChoice;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * chatgpt的交互封装集成
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class ChatGptIntegration {
    @Autowired
    private ChatGptConfig config;

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "chatgpt")
    public static class ChatGptConfig {
        /**
         * 默认的模型
         */
        private AISourceEnum main;
        private Map<AISourceEnum, GptConf> conf;
    }

    @Data
    public static class GptConf {
        private List<String> keys;
        private boolean proxy;
        private String apiHost;
        private int timeOut;
        private int maxToken;

        public String fetchKey() {
            int index = RandomUtil.randomInt(keys.size());
            return keys.get(index);
        }
    }

    public static ChatCompletion.Model parse2GptMode(AISourceEnum model) {
        if (model == AISourceEnum.CHAT_GPT_4) {
            return ChatCompletion.Model.GPT_4;
        }
        return ChatCompletion.Model.GPT_3_5_TURBO;
    }

    @PostConstruct
    public void init() {
        log.info("ChatGpt配置初始化完成: {}", config);
    }

    /**
     * 每个用户的会话缓存
     */
    public LoadingCache<ImmutablePair<Long, AISourceEnum>, ImmutablePair<ChatGPT, ChatGPTStream>> cacheStream;

    @PostConstruct
    public void initKey() {
        cacheStream = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.SECONDS)
                .build(new CacheLoader<ImmutablePair<Long, AISourceEnum>, ImmutablePair<ChatGPT, ChatGPTStream>>() {
                    @Override
                    public ImmutablePair<ChatGPT, ChatGPTStream> load(ImmutablePair<Long, AISourceEnum> s) throws Exception {
                        return ImmutablePair.of(null, null);
                    }
                });
    }

    /**
     * 基于routingkey进行路由，创建一个简单的GPTClient
     *
     * @param routingKey
     * @return
     */
    private ChatGPT simpleGPT(Long routingKey, AISourceEnum model) {
        GptConf conf = config.getConf().getOrDefault(model, config.getConf().get(config.getMain()));
        Proxy proxy = conf.isProxy() ? ProxyCenter.loadProxy(String.valueOf(routingKey)) : Proxy.NO_PROXY;

        return ChatGPT.builder().apiKeyList(conf.getKeys()).proxy(proxy).apiHost(conf.getApiHost()) //反向代理地址
                .timeout(conf.getTimeOut()).build().init();
    }

    /**
     * 基于routingkey进行路由，创建一个简单的流式GPTClientStream
     *
     * @param routingKey
     * @return
     */
    private ChatGPTStream simpleStreamGPT(Long routingKey, AISourceEnum model) {
        GptConf conf = config.getConf().getOrDefault(model, config.getConf().get(config.getMain()));
        Proxy proxy = conf.isProxy() ? ProxyCenter.loadProxy(String.valueOf(routingKey)) : Proxy.NO_PROXY;

        return ChatGPTStream.builder().timeout(conf.getTimeOut()).apiKey(conf.fetchKey()).proxy(proxy)
                .apiHost(conf.getApiHost()).build().init();
    }

    public ChatGPT getGpt(Long routingKey, AISourceEnum model) {
        ImmutablePair<Long, AISourceEnum> key = ImmutablePair.of(routingKey, model);
        ImmutablePair<ChatGPT, ChatGPTStream> pair = cacheStream.getUnchecked(key);
        ChatGPT gpt = pair.left;
        if (gpt == null) {
            gpt = simpleGPT(routingKey, model);
            cacheStream.put(key, ImmutablePair.of(gpt, pair.right));
        }
        return gpt;
    }

    public ChatGPTStream getGptStream(Long routingKey, AISourceEnum model) {
        ImmutablePair<Long, AISourceEnum> key = ImmutablePair.of(routingKey, model);
        ImmutablePair<ChatGPT, ChatGPTStream> pair = cacheStream.getUnchecked(key);
        ChatGPTStream gpt = pair.right;
        if (gpt == null) {
            gpt = simpleStreamGPT(routingKey, model);
            cacheStream.put(key, ImmutablePair.of(pair.left, gpt));
        }
        return gpt;
    }

    /**
     * 账户信息
     *
     * @return
     */
    public CreditGrantsResponse creditInfo(AISourceEnum model) {
        CreditGrantsResponse response = getGpt(0L, model).creditGrants();
        return response;
    }

    public boolean directReturn(Long routingKey, ChatItemVo chat) {
        AISourceEnum selectModel = config.getMain();
        GptConf conf = config.getConf().getOrDefault(selectModel, config.getConf().get(config.getMain()));
        ChatGPT gpt = getGpt(routingKey, config.getMain());
        try {
            ChatCompletion chatCompletion = ChatCompletion.builder().model(parse2GptMode(selectModel).getName())
                    .messages(Arrays.asList(Message.of(chat.getQuestion()))).maxTokens(conf.getMaxToken()).build();
            ChatCompletionResponse response = gpt.chatCompletion(chatCompletion);
            List<ChatChoice> list = response.getChoices();
            chat.initAnswer(JsonUtil.toStr(list), ChatAnswerTypeEnum.JSON);
            log.info("chatgpt试用! 传参:{}, 返回:{}", chat, list);
            return true;
        } catch (Exception e) {
            // 对于系统异常，不用继续等待了
            chat.initAnswer(e.getMessage());
            log.info("chatgpt执行异常！ key:{}", chat, e);
            return false;
        }
    }

    /**
     * 异步流式返回
     *
     * @param routingKey
     * @param chat
     * @param listener
     * @return
     */
    public boolean streamReturn(Long routingKey, ChatItemVo chat, EventSourceListener listener) {
        AISourceEnum selectModel = config.getMain();
        GptConf conf = config.getConf().getOrDefault(selectModel, config.getConf().get(config.getMain()));
        ChatGPTStream chatGPTStream = simpleStreamGPT(routingKey, selectModel);

        ChatCompletion chatCompletion = ChatCompletion.builder().model(parse2GptMode(selectModel).getName())
                .messages(Arrays.asList(Message.of(chat.getQuestion()))).maxTokens(conf.getMaxToken()).build();
        chatGPTStream.streamChatCompletion(chatCompletion, listener);
        return true;
    }

    /**
     * 一个基础的chatgpt问答， 给微信公众号自动问答使用
     *
     * @param routingKey
     * @param record
     * @return
     */
    public boolean directReturn(Long routingKey, ChatRecordWxVo record) {
        AISourceEnum selectModel = config.getMain();
        GptConf conf = config.getConf().getOrDefault(selectModel, config.getConf().get(config.getMain()));
        ChatGPT gpt = getGpt(routingKey, config.getMain());
        try {
            ChatCompletion chatCompletion = ChatCompletion.builder().model(parse2GptMode(selectModel).getName())
                    .messages(Arrays.asList(Message.of(record.getQas()))).maxTokens(conf.getMaxToken()).build();
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
