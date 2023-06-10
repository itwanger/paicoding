package com.github.paicoding.forum.service.chatgpt.service;

import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatgpt.constants.ChatConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 聊天的抽象模板类
 *
 * @author YiHui
 * @date 2023/6/9
 */
@Slf4j
@Service
public abstract class AbsChatService implements ChatService {
    private ExecutorService asyncExecutors = Executors.newFixedThreadPool(10);


    /**
     * 查询已经使用的次数
     *
     * @param user
     * @return
     */
    protected int queryUserdCnt(String user) {
        Integer cnt = RedisClient.hGet(ChatConstants.getAiRateKey(source()), user, Integer.class);
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
    protected Long incrCnt(String user) {
        return RedisClient.hIncr(ChatConstants.getAiRateKey(source()), user, 1);
    }

    /**
     * 保存聊天记录
     */
    protected void recordChatItem(String user, ChatItemVo item) {
        RedisClient.lPush(ChatConstants.getAiHistoryRecordsKey(source(), user), item);
    }

    /**
     * 查询用户的聊天历史
     *
     * @return
     */
    public ChatRecordsVo getChatHistory(String user) {
        List<ChatItemVo> chats = RedisClient.lRange(ChatConstants.getAiHistoryRecordsKey(source(), user), 0, 50, ChatItemVo.class);
        chats.add(0, new ChatItemVo().initAnswer("开始你和派聪明的AI之旅吧!"));
        ChatRecordsVo vo = new ChatRecordsVo();
        vo.setMaxCnt(getMaxQaCnt(user));
        vo.setUsedCnt(queryUserdCnt(user));
        vo.setSource(source());
        vo.setRecords(chats);
        return vo;
    }

    @Override
    public ChatRecordsVo chat(String user, String question) {
        ChatRecordsVo res = initResVo(user);
        ChatItemVo itemVo = new ChatItemVo().initQuestion(question);
        if (res.getUsedCnt() >= res.getMaxCnt()) {
            // 使用次数已经用完了
            res.getRecords().add(itemVo.initAnswer("您的免费次数已经使用完毕了!"));
            return res;
        }

        answer(user, itemVo, res);
        return res;
    }

    private ChatRecordsVo initResVo(String user) {
        ChatRecordsVo res = new ChatRecordsVo();
        res.setSource(source());
        int maxCnt = getMaxQaCnt(user);
        int usedCnt = queryUserdCnt(user);
        res.setMaxCnt(maxCnt);
        res.setUsedCnt(usedCnt);
        res.setRecords(new ArrayList<>(1));
        return res;
    }

    protected void answer(String user, ChatItemVo itemVo, ChatRecordsVo res) {
        boolean ans = doAnswer(user, itemVo);
        res.getRecords().add(itemVo);
        if (ans) {
            // 回答成功，保存聊天记录，剩余次数-1
            incrCnt(user);
            recordChatItem(user, itemVo);
            res.setUsedCnt(res.getUsedCnt() + 1);
            if (res.getUsedCnt() > ChatConstants.MAX_HISTORY_RECORD_ITEMS) {
                // 最多保存五百条历史聊天记录
                RedisClient.lTrim(ChatConstants.getAiHistoryRecordsKey(source(), user), 0, ChatConstants.MAX_HISTORY_RECORD_ITEMS);
            }
        }
    }

    /**
     * 提问，并将结果写入chat
     *
     * @param user
     * @param chat
     * @return true 表示正确回答了； false 表示回答出现异常
     */
    public abstract boolean doAnswer(String user, ChatItemVo chat);

    /**
     * 查询当前用户最多可提问的次数
     *
     * @param user
     * @return
     */
    protected int getMaxQaCnt(String user) {
        return ChatConstants.MAX_CHATGPT_QAS_CNT;
    }

    @Override
    public ChatRecordsVo asyncChat(String user, String question, Consumer<ChatRecordsVo> consumer) {
        ChatRecordsVo res = initResVo(user);
        ChatItemVo itemVo = new ChatItemVo().initQuestion(question);
        if (res.getUsedCnt() >= res.getMaxCnt()) {
            // 使用次数已经用完了
            res.getRecords().add(itemVo.initAnswer("您的免费次数已经使用完毕了!"));
            return res;
        }

        asyncExecutors.execute(new Runnable() {
            @Override
            public void run() {
                log.info("异步聊天回复");
                ChatRecordsVo vo = res.clone();
                ChatItemVo newItemVo = itemVo.clone();
                try {
                    // 模拟，延时1.2s之后才返回结果
                    Thread.sleep(1200);
                } catch (Exception e) {
                }
                answer(user, newItemVo, vo);
                consumer.accept(vo);
            }
        });

        res.getRecords().add(itemVo.initAnswer("小派正在努力回答中, 耐心等待一下吧..."));
        return res;
    }
}
