package com.github.paicoding.forum.service.chatgpt.service;

import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatgpt.constants.ChatConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/6/9
 */
@Service
public abstract class AbsChatService implements ChatService {

    /**
     * 查询已经使用的次数
     *
     * @param userId
     * @return
     */
    protected int queryUserdCnt(String userId) {
        Integer cnt = RedisClient.hGet(ChatConstants.getAiRateKey(source()), userId, Integer.class);
        if (cnt == null) {
            cnt = 0;
        }
        return cnt;
    }


    /**
     * 使用次数+1
     *
     * @param userId
     * @return
     */
    protected Long incrCnt(String userId) {
        return RedisClient.hIncr(ChatConstants.getAiRateKey(source()), userId, 1);
    }

    /**
     * 保存聊天记录
     */
    protected void recordChatItem(String userId, ChatItemVo item) {
        RedisClient.lPush(ChatConstants.getAiHistoryRecordsKey(source(), userId), item);
    }

    /**
     * 查询用户的聊天历史
     *
     * @return
     */
    public ChatRecordsVo getChatHistory(String userId) {
        List<ChatItemVo> chats = RedisClient.lRange(ChatConstants.getAiHistoryRecordsKey(source(), userId), 0, 50, ChatItemVo.class);
        chats.add(0, new ChatItemVo().initAnswer("开始我们的聊天之旅吧!"));
        ChatRecordsVo vo = new ChatRecordsVo();
        vo.setMaxCnt(50);
        vo.setAvaliableCnt(vo.getMaxCnt() - queryUserdCnt(userId));
        vo.setSource(source());
        vo.setRecords(chats);
        return vo;
    }

    @Override
    public ChatRecordsVo chat(String user, String question) {
        ChatRecordsVo res = initResVo(user);
        ChatItemVo itemVo = new ChatItemVo().initQuestion(question);
        if (res.getAvaliableCnt() <= 0) {
            // 使用次数已经用完了
            res.getRecords().add(itemVo.initAnswer("您的免费次数已经使用完毕了!"));
            return res;
        }

        boolean ans = answer(user, itemVo);
        res.getRecords().add(itemVo);
        if (ans) {
            // 回答成功，保存聊天记录，剩余次数-1
            incrCnt(user);
            recordChatItem(user, itemVo);
            res.setAvaliableCnt(res.getAvaliableCnt() - 1);
        }
        return res;
    }

    private ChatRecordsVo initResVo(String user) {
        ChatRecordsVo res = new ChatRecordsVo();
        res.setSource(source());
        int maxCnt = getMaxQaCnt(user);
        int usedCnt = queryUserdCnt(user);
        res.setMaxCnt(maxCnt);
        res.setAvaliableCnt(Math.max(0, maxCnt - usedCnt));
        res.setRecords(new ArrayList<>(1));
        return res;
    }

    public abstract boolean answer(String user, ChatItemVo chat);

    protected int getMaxQaCnt(String user) {
        return ChatConstants.MAX_CHATGPT_QAS_CNT;
    }
}
