package com.github.paicoding.forum.service.chatv2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.chatv2.repository.entity.ChatMessageDO;
import com.github.paicoding.forum.service.chatv2.repository.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Chat Memory 服务 - 实现 Spring AI 的 ChatMemory 接口
 * 使用 MySQL 存储会话记忆
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryService implements ChatMemory {

    private final ChatMessageMapper chatMessageMapper;

    /**
     * 最大消息数量限制
     */
    private static final int MAX_MESSAGES = 100;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isEmpty()) {
            log.warn("Attempted to add messages with null or empty conversationId, skipping");
            return;
        }

        log.debug("Adding {} messages to conversation: {}", messages.size(), conversationId);

        // conversationId 格式: historyId
        Long historyId;
        try {
            historyId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            log.error("Invalid conversationId format: {}, expected a number", conversationId, e);
            return;
        }

        // 获取当前最大序号
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId)
                .orderByDesc(ChatMessageDO::getSequenceNum)
                .last("LIMIT 1");
        ChatMessageDO lastMessage = chatMessageMapper.selectOne(wrapper);
        int nextSeq = (lastMessage == null) ? 0 : lastMessage.getSequenceNum() + 1;

        // 保存消息
        for (Message message : messages) {
            ChatMessageDO messageDO = new ChatMessageDO();
            messageDO.setHistoryId(historyId);
            messageDO.setRole(getRoleFromMessage(message));
            messageDO.setContent(message.getText());
            messageDO.setSequenceNum(nextSeq++);
            messageDO.setTimestamp(new Date());

            chatMessageMapper.insert(messageDO);
        }

        log.debug("Added {} messages to conversation {}", messages.size(), conversationId);
    }

    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, MAX_MESSAGES);
    }

    public List<Message> get(String conversationId, int lastN) {
        if (conversationId == null || conversationId.isEmpty()) {
            log.warn("Attempted to get messages with null or empty conversationId, returning empty list");
            return new ArrayList<>();
        }

        log.debug("Getting last {} messages from conversation: {}", lastN, conversationId);

        Long historyId;
        try {
            historyId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            log.error("Invalid conversationId format: {}, expected a number, returning empty list", conversationId, e);
            return new ArrayList<>();
        }

        // 查询最近的 N 条消息
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId)
                .orderByDesc(ChatMessageDO::getSequenceNum)
                .last("LIMIT " + Math.min(lastN, MAX_MESSAGES));

        List<ChatMessageDO> messageDOs = chatMessageMapper.selectList(wrapper);

        // 转换为 Spring AI Message 并反转顺序（因为查询是倒序的）
        List<Message> messages = new ArrayList<>();
        for (int i = messageDOs.size() - 1; i >= 0; i--) {
            ChatMessageDO messageDO = messageDOs.get(i);
            messages.add(convertToMessage(messageDO));
        }

        log.debug("Retrieved {} messages from conversation {}", messages.size(), conversationId);
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            log.warn("Attempted to clear messages with null or empty conversationId, skipping");
            return;
        }

        Long historyId;
        try {
            historyId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            log.error("Invalid conversationId format: {}, expected a number", conversationId, e);
            return;
        }

        // 软删除所有消息
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId);

        ChatMessageDO updateDO = new ChatMessageDO();
        updateDO.setDeleted(1);
        chatMessageMapper.update(updateDO, wrapper);

        log.info("Cleared all messages for conversation {}", conversationId);
    }

    /**
     * 获取指定会话的最新 assistant 消息 ID
     *
     * @param historyId 会话ID
     * @return 最新 assistant 消息的数据库 ID，如果没有则返回 null
     */
    public Long getLatestAssistantMessageId(Long historyId) {
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId)
                .eq(ChatMessageDO::getRole, "assistant")
                .orderByDesc(ChatMessageDO::getSequenceNum)
                .last("LIMIT 1");

        ChatMessageDO messageDO = chatMessageMapper.selectOne(wrapper);
        return messageDO != null ? messageDO.getId() : null;
    }

    /**
     * 获取角色名称
     */
    private String getRoleFromMessage(Message message) {
        if (message instanceof UserMessage) {
            return "user";
        } else if (message instanceof AssistantMessage) {
            return "assistant";
        } else if (message instanceof SystemMessage) {
            return "system";
        } else {
            return "tool";
        }
    }

    /**
     * 转换为 Spring AI Message
     */
    private Message convertToMessage(ChatMessageDO messageDO) {
        String content = messageDO.getContent();
        return switch (messageDO.getRole()) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> new AssistantMessage(content); // 默认为 assistant
        };
    }
}
