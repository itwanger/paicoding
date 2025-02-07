package com.github.paicoding.forum.service.chatai.service.history;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatSessionItemVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.service.ChatHistoryService;
import com.github.paicoding.forum.service.user.service.UserAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对话历史记录
 *
 * @author YiHui
 * @date 2025/2/7
 */
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    @Autowired
    private UserAiService userAiService;

    @Override
    public List<ChatSessionItemVo> listChatSessions(AISourceEnum source, Long userId) {
        String key = ChatConstants.getAiChatListKey(source, userId);
        Map<String, ChatSessionItemVo> map = RedisClient.hGetAll(key, ChatSessionItemVo.class);
        return new ArrayList<>(map.values());
    }

    @Override
    public List<ChatItemVo> listHistory(AISourceEnum source, Long userId, String chatId, Integer size) {
        size = size == null ? 50 : size;
        return RedisClient.lRange(getChatIdKey(source, userId, chatId), 0, size, ChatItemVo.class);
    }

    @Override
    public void saveRecord(AISourceEnum source, Long userId, String chatId, ChatItemVo item) {
        // 写入 MySQL
        userAiService.pushChatItem(source, userId, item);

        // 更新redis缓存数据
        String key = getChatIdKey(source, userId, chatId);
        RedisClient.lPush(key, item);

        // 维护对话记录
        String sessionKey = ChatConstants.getAiChatListKey(source, userId);
        ChatSessionItemVo session = RedisClient.hGet(sessionKey, chatId, ChatSessionItemVo.class);
        if (session == null) {
            session = new ChatSessionItemVo();
            session.setChatId(chatId);
            session.setTitle(item.getQuestion());
            session.setCreatTime(System.currentTimeMillis());
            session.setUpdateTime(session.getCreatTime());
            session.setQasCnt(1);
        } else {
            session.setUpdateTime(System.currentTimeMillis());
            session.setQasCnt(session.getQasCnt() + 1);
        }
        RedisClient.hSet(sessionKey, chatId, session);

        if (session.getQasCnt() > ChatConstants.MAX_HISTORY_RECORD_ITEMS) {
            // 最多保存五百条历史聊天记录
            RedisClient.lTrim(key, 0, ChatConstants.MAX_HISTORY_RECORD_ITEMS);
        }

    }

    private String getChatIdKey(AISourceEnum source, Long userId, String chatId) {
        return StringUtils.isBlank(chatId) ? ChatConstants.getAiHistoryRecordsKey(source, userId) : ChatConstants.getAiHistoryRecordsKey(source, userId + ":" + chatId);
    }
}
