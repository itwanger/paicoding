package com.github.paicoding.forum.service.chatai.service.history;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatSessionItemVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.service.ChatHistoryService;
import com.github.paicoding.forum.service.user.service.UserAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Autowired
    private AiBots aiBots;

    /**
     * 列出聊天会话
     * <p>
     * 根据用户ID和AI源枚举获取聊天会话列表从Redis中通过哈希结构存储的键值对获取所有会话项，
     * 并按更新时间降序排序返回
     *
     * @param source AI源枚举，用于区分不同的AI来源
     * @param userId 用户ID，用于获取特定用户的聊天会话
     * @return 返回一个ChatSessionItemVo对象列表，包含用户的聊天会话项
     */
    @Override
    public List<ChatSessionItemVo> listChatSessions(AISourceEnum source, Long userId) {
        // 构造Redis中哈希结构的键
        String key = ChatConstants.getAiChatListKey(source, userId);
        // 从Redis中获取所有会话项，使用hGetAll方法获取哈希表中所有的字段和值
        Map<String, ChatSessionItemVo> map = RedisClient.hGetAll(key, ChatSessionItemVo.class);
        // 将Map中的值转换为List
        List<ChatSessionItemVo> list = new ArrayList<>(map.values());
        // 对列表按更新时间降序排序
        list.sort((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));
        // 返回排序后的列表
        return list;
    }

    @Override
    public List<ChatItemVo> listHistory(AISourceEnum source, Long userId, String chatId, Integer size) {
        size = size == null ? 50 : size;
        List<ChatItemVo> list = RedisClient.lRange(getChatIdKey(source, userId, chatId), 0, size, ChatItemVo.class);

        // 对于特殊的交互机器人，自动补齐相关的提示词
        ChatItemVo prompt = aiBots.autoBuildPrompt(userId, chatId);
        if (prompt != null) {
            list.add(0, prompt);
        }
        return list;
    }

    /**
     * 保存聊天记录
     *
     * @param source 聊天来源，用于区分不同的聊天场景或平台
     * @param userId 用户ID，用于关联用户信息
     * @param chatId 聊天ID，用于标识特定的聊天会话
     * @param item   聊天项内容，包括用户的问题和AI的回答
     */
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
            // 如果当前会话不存在，则创建新会话记录
            session = new ChatSessionItemVo();
            session.setChatId(chatId);
            session.setTitle(!item.getQuestion().startsWith(ChatConstants.PROMPT_TAG) ? item.getQuestion() : item.getQuestion().substring(ChatConstants.PROMPT_TAG.length()));
            session.setCreatTime(System.currentTimeMillis());
            session.setUpdateTime(session.getCreatTime());
            session.setQasCnt(1);
        } else {
            // 如果会话已存在，则更新会话记录
            session.setUpdateTime(System.currentTimeMillis());
            session.setQasCnt(session.getQasCnt() + 1);
        }
        RedisClient.hSet(sessionKey, chatId, session);

        // 限制对话记录数量，最多保存五百条历史聊天记录
        if (session.getQasCnt() > ChatConstants.MAX_HISTORY_RECORD_ITEMS) {
            RedisClient.lTrim(key, 0, ChatConstants.MAX_HISTORY_RECORD_ITEMS);
        }

    }

    /**
     * 更新聊天会话的名称
     *
     * @param source 聊天会话的来源，用于区分不同的AI来源
     * @param chatId 聊天会话的唯一标识符
     * @param title  新的聊天会话名称
     * @param userId 用户的唯一标识符
     * @return 如果会话名称被更新，则返回true；否则返回false
     */
    @Override
    public Boolean updateChatSessionName(AISourceEnum source, String chatId, String title, Long userId) {
        // 构造Redis中存储聊天列表的键
        String key = ChatConstants.getAiChatListKey(source, userId);

        // 从Redis中获取指定聊天会话的详细信息
        ChatSessionItemVo item = RedisClient.hGet(key, chatId, ChatSessionItemVo.class);

        // 检查获取到的聊天会话信息是否不为空，并且新标题与旧标题不同
        if (item != null && !Objects.equals(item.getTitle(), title)) {
            // 更新聊天会话的标题
            item.setTitle(title);

            // 将更新后的聊天会话信息保存回Redis
            return RedisClient.hSet(key, chatId, item);
        }
        // 如果聊天会话信息未更改，则直接返回true
        return true;
    }

    /**
     * 重写移除聊天会话的方法
     *
     * @param source 数据源枚举，用于区分不同的AI来源
     * @param chatId 聊天会话的唯一标识符
     * @param userId 用户的唯一标识符
     * @return 返回操作的布尔结果，表示是否成功移除会话
     */
    @Override
    public Boolean removeChatSession(AISourceEnum source, String chatId, Long userId) {
        // 构造Redis中AI聊天列表的键
        String key = ChatConstants.getAiChatListKey(source, userId);
        // 使用Redis的hDel命令移除指定的聊天会话，并返回操作结果
        RedisClient.hDel(key, chatId);
        return true;
    }

    private String getChatIdKey(AISourceEnum source, Long userId, String chatId) {
        return StringUtils.isBlank(chatId) ? ChatConstants.getAiHistoryRecordsKey(source, userId) : ChatConstants.getAiHistoryRecordsKey(source, userId + ":" + chatId);
    }
}
