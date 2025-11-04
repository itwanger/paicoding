package com.github.paicoding.forum.service.chatai.bot;

import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author YiHui
 * @date 2025/2/24
 */
@Service
public class AiBots {
    @Autowired
    private AiBotService botService;

    /**
     * 系统提示词缓存
     */
    private static final LoadingCache<ImmutablePair<AiBotEnum, String>, Supplier<String>> systemPromptCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<ImmutablePair<AiBotEnum, String>, Supplier<String>>() {
                @Override
                public Supplier<String> load(ImmutablePair<AiBotEnum, String> key) throws Exception {
                    return () -> key.getLeft().getPrompt();
                }
            });

    /**
     * 判断目标用户是否为AI机器人
     *
     * @param userId
     * @return
     */
    public boolean aiBots(Long userId) {
        return botService.getBotEnumByUserId(userId) != null;
    }

    /**
     * 自动补齐AI机器人的提示词
     *
     * @param userId
     * @return
     */
    public ChatItemVo autoBuildPrompt(Long userId, String chatId) {
        AiBotEnum bot = botService.getBotEnumByUserId(userId);
        if (bot == null) {
            return null;
        }

        // 构建系统提示词
        String promptContent = systemPromptCache.getUnchecked(ImmutablePair.of(bot, chatId)).get();
        String prompt = ChatConstants.PROMPT_TAG + promptContent;
        return new ChatItemVo().setQuestion(prompt);
    }


    /**
     * 判断是否触发了AI机器人的关键词；如果触发，则返回对应的Ai机器人
     *
     * @param comment 评论的文本内容
     * @return null 表示没有触发；否则表示已经触发
     */
    public AiBotEnum triggerAiBotKeyWord(String comment) {
        for (AiBotEnum bot : AiBotEnum.values()) {
            String tag = "@" + bot.getNickName();
            if (comment.contains(tag)) {
                return bot;
            }
        }
        return null;
    }

    public AiBotEnum getAiBotByUserId(Long userId) {
        return botService.getBotEnumByUserId(userId);
    }

    public BaseUserInfoDTO getBotUser(AiBotEnum bot) {
        return botService.getBotEnumByUserId(bot);
    }

    public void trigger(AiBotEnum bot, String question, String sourceBizId, Consumer<String> consumer, Supplier<String> systemPromptGenerator) {
        // 支持自定义的ai机器人系统提示词注入
        systemPromptCache.put(ImmutablePair.of(bot, sourceBizId), systemPromptGenerator);
        // 触发AI机器人的交互
        botService.trigger(bot, question, sourceBizId, consumer);
    }
}
