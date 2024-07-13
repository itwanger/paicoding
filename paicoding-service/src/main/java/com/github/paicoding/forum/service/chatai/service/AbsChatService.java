package com.github.paicoding.forum.service.chatai.service;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.user.service.UserAiService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 聊天的抽象模板类
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public abstract class AbsChatService implements ChatService {
    @Autowired
    private UserAiService userAiService;
    @Autowired
    private SensitiveService sensitiveService;


    /**
     * 查询已经使用的次数
     *
     * @param user
     * @return
     */
    protected int queryUserdCnt(Long user) {
        Integer cnt = RedisClient.hGet(ChatConstants.getAiRateKeyPerDay(source()), String.valueOf(user), Integer.class);
        if (cnt == null) {
            cnt = 0;
        }
        return cnt;
    }


    /**
     * 使用次数+1
     *
     * @param user
     * @return
     */
    protected Long incrCnt(Long user) {
        String key = ChatConstants.getAiRateKeyPerDay(source());
        Long cnt = RedisClient.hIncr(key, String.valueOf(user), 1);
        if (cnt == 1L) {
            // 做一个简单的判定，如果是某个用户的第一次提问，那就刷新一下这个缓存的有效期
            // fixme 这里有个不太优雅的地方：每新来一个用户，会导致这个有效期重新刷一边，可以通过再查一下hash的key个数，如果只有一个才进行重置有效期；这里出于简单考虑，省了这一步
            RedisClient.expire(key, 86400L);
        }
        return cnt;
    }

    /**
     * 保存聊天记录
     */
    protected void recordChatItem(Long user, ChatItemVo item) {
        // 写入 MySQL
        userAiService.pushChatItem(source(), user, item);

        // 写入 Redis
        RedisClient.lPush(ChatConstants.getAiHistoryRecordsKey(source(), user), item);
    }

    /**
     * 查询用户的聊天历史
     *
     * @return
     */
    public ChatRecordsVo getChatHistory(Long user, AISourceEnum aiSource) {
        if (aiSource == null) {
            aiSource = source();
        }
        List<ChatItemVo> chats = RedisClient.lRange(ChatConstants.getAiHistoryRecordsKey(aiSource, user), 0, 50, ChatItemVo.class);
        chats.add(0, new ChatItemVo().initAnswer(String.format("开始你和派聪明(%s-大模型)的AI之旅吧!", aiSource.getName())));
        ChatRecordsVo vo = new ChatRecordsVo();
        vo.setMaxCnt(getMaxQaCnt(user));
        vo.setUsedCnt(queryUserdCnt(user));
        vo.setSource(source());
        vo.setRecords(chats);
        return vo;
    }

    @Override
    public ChatRecordsVo chat(Long user, String question) {
        // 构建提问、返回的实体类，计算使用次数，最大次数
        ChatRecordsVo res = initResVo(user, question);
        if (!res.hasQaCnt()) {
            return res;
        }

        // 执行提问
        answer(user, res);
        // 返回AI应答结果
        return res;
    }

    @Override
    public ChatRecordsVo chat(Long user, String question, Consumer<ChatRecordsVo> consumer) {
        ChatRecordsVo res = initResVo(user, question);
        if (!res.hasQaCnt()) {
            return res;
        }

        // 同步聊天时，直接返回结果
        answer(user, res);
        consumer.accept(res);
        return res;
    }

    private ChatRecordsVo initResVo(Long user, String question) {
        ChatRecordsVo res = new ChatRecordsVo();
        res.setSource(source());
        int maxCnt = getMaxQaCnt(user);
        int usedCnt = queryUserdCnt(user);
        res.setMaxCnt(maxCnt);
        res.setUsedCnt(usedCnt);

        ChatItemVo item = new ChatItemVo().initQuestion(question);
        if (!res.hasQaCnt()) {
            // 次数已经使用完毕
            item.initAnswer(ChatConstants.TOKEN_OVER);
        }
        res.setRecords(Arrays.asList(item));
        return res;
    }

    protected AiChatStatEnum answer(Long user, ChatRecordsVo res) {
        ChatItemVo itemVo = res.getRecords().get(0);
        AiChatStatEnum ans;
        List<String> sensitiveWords = sensitiveService.contains(itemVo.getQuestion());
        if (!CollectionUtils.isEmpty(sensitiveWords)) {
            itemVo.initAnswer(String.format(ChatConstants.SENSITIVE_QUESTION, sensitiveWords));
            ans = AiChatStatEnum.ERROR;
        } else {
            ans = doAnswer(user, itemVo);
            if (ans == AiChatStatEnum.END) {
                processAfterSuccessedAnswered(user, res);
            }
        }
        return ans;
    }

    /**
     * 提问，并将结果写入chat
     *
     * @param user
     * @param chat
     * @return true 表示正确回答了； false 表示回答出现异常
     */
    public abstract AiChatStatEnum doAnswer(Long user, ChatItemVo chat);

    /**
     * 成功返回之后的后置操作
     *
     * @param user
     * @param response
     */
    protected void processAfterSuccessedAnswered(Long user, ChatRecordsVo response) {
        // 回答成功，保存聊天记录，剩余次数-1
        response.setUsedCnt(incrCnt(user).intValue());
        recordChatItem(user, response.getRecords().get(0));
        if (response.getUsedCnt() > ChatConstants.MAX_HISTORY_RECORD_ITEMS) {
            // 最多保存五百条历史聊天记录
            RedisClient.lTrim(ChatConstants.getAiHistoryRecordsKey(source(), user), 0, ChatConstants.MAX_HISTORY_RECORD_ITEMS);
        }
    }

    /**
     * 异步聊天，即提问并不要求直接得到接口；等后台准备完毕之后再写入对应的结果
     *
     * @param user
     * @param question
     * @param consumer 执行成功之后，直接异步回调的通知
     * @return
     */
    @Override
    public ChatRecordsVo asyncChat(Long user, String question, Consumer<ChatRecordsVo> consumer) {
        ChatRecordsVo res = initResVo(user, question);
        if (!res.hasQaCnt()) {
            // 次数使用完毕
            consumer.accept(res);
            return res;
        }

        List<String> sensitiveWord = sensitiveService.contains(res.getRecords().get(0).getQuestion());
        if (!CollectionUtils.isEmpty(sensitiveWord)) {
            // 包含敏感词的提问，直接返回异常
            res.getRecords().get(0).initAnswer(String.format(ChatConstants.SENSITIVE_QUESTION, sensitiveWord));
            consumer.accept(res);
        } else {
            final ChatRecordsVo newRes = res.clone();
            AiChatStatEnum needReturn = doAsyncAnswer(user, newRes, (ans, vo) -> {
                if (ans == AiChatStatEnum.END) {
                    // 只有最后一个会话，即ai的回答结束，才需要进行持久化，并计数
                    processAfterSuccessedAnswered(user, newRes);
                } else if (ans == AiChatStatEnum.ERROR) {
                    // 执行异常，更新AI模型
                    SpringUtil.getBean(ChatFacade.class).refreshAiSourceCache(Sets.newHashSet(source()));
                }
                // ai异步返回结果之后，我们将结果推送给前端用户
                consumer.accept(newRes);
            });

            if (needReturn.needResponse()) {
                // 异步响应时，为了避免长时间的等待，这里直接响应用户的提问，返回一个稍等得提示文案
                ChatItemVo nowItem = res.getRecords().get(0);
                nowItem.initAnswer(ChatConstants.ASYNC_CHAT_TIP);
                consumer.accept(res);
            }
        }
        return res;
    }

    /**
     * 异步返回结果
     *
     * @param user
     * @param response 保存提问 & 返回的结果，最终会返回给前端用户
     * @param consumer 具体将 response 写回前端的实现策略
     * @return 返回的会话状态，控制是否需要将结果直接返回给前端
     */
    public abstract AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo response, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer);

    /**
     * 查询当前用户最多可提问的次数
     *
     * @param user
     * @return
     */
    protected int getMaxQaCnt(Long user) {
        return userAiService.getMaxChatCnt(user);
    }
}
