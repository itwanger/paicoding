package com.github.paicoding.forum.service.chatv2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.chatv2.repository.entity.ChatHistoryDO;
import com.github.paicoding.forum.service.chatv2.repository.mapper.ChatHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 会话管理服务
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConversationService {

    private final ChatHistoryMapper chatHistoryMapper;

    /**
     * 使用指定的 conversationId 创建新会话
     *
     * @param userId 用户ID
     * @param modelName 模型名称
     * @param conversationId 会话ID（后端生成的UUID）
     * @return 会话DO
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatHistoryDO createConversationWithConversationId(Long userId, String modelName, String conversationId) {
        ChatHistoryDO history = new ChatHistoryDO();
        history.setConversationId(conversationId);
        history.setUserId(userId);
        history.setModelName(modelName);
        history.setTitle("新对话");
        history.setTitleGeneratedBy("auto");
        history.setCreateTime(new Date());
        history.setUpdateTime(new Date());

        chatHistoryMapper.insert(history);

        log.info("Created new conversation with specified conversationId: conversationId={}, userId={}, modelName={}",
                conversationId, userId, modelName);

        return history;
    }

    /**
     * 更新会话标题（通过主键ID）
     *
     * @param historyId 会话ID
     * @param title 新标题
     * @param generatedBy 生成方式
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTitle(Long historyId, String title, String generatedBy) {
        ChatHistoryDO history = new ChatHistoryDO();
        history.setId(historyId);
        history.setTitle(title);
        history.setTitleGeneratedBy(generatedBy);
        history.setUpdateTime(new Date());

        chatHistoryMapper.updateById(history);

        log.info("Updated conversation title: historyId={}, title={}", historyId, title);
    }

    /**
     * 更新会话标题（通过 conversationId）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID（用于权限检查）
     * @param title 新标题
     * @param generatedBy 生成方式
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTitleByConversationId(String conversationId, Long userId, String title, String generatedBy) {
        // 先查找会话
        ChatHistoryDO history = getConversationByConversationId(conversationId, userId);
        if (history == null) {
            log.warn("Conversation not found or unauthorized: conversationId={}, userId={}", conversationId, userId);
            return false;
        }

        // 更新标题
        ChatHistoryDO updateDO = new ChatHistoryDO();
        updateDO.setId(history.getId());
        updateDO.setTitle(title);
        updateDO.setTitleGeneratedBy(generatedBy);
        updateDO.setUpdateTime(new Date());

        chatHistoryMapper.updateById(updateDO);

        log.info("Updated conversation title by conversationId: conversationId={}, historyId={}, title={}", conversationId, history.getId(), title);
        return true;
    }

    /**
     * 更新会话时间
     *
     * @param historyId 会话ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConversationTime(Long historyId) {
        ChatHistoryDO history = new ChatHistoryDO();
        history.setId(historyId);
        history.setUpdateTime(new Date());

        chatHistoryMapper.updateById(history);
    }

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<ChatHistoryDO> getConversationsByUser(Long userId) {
        LambdaQueryWrapper<ChatHistoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistoryDO::getUserId, userId)
                .orderByDesc(ChatHistoryDO::getUpdateTime);

        return chatHistoryMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取会话
     *
     * @param historyId 会话ID
     * @return 会话DO
     */
    public ChatHistoryDO getConversationById(Long historyId) {
        return chatHistoryMapper.selectById(historyId);
    }

    /**
     * 根据 conversationId 和用户ID获取会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话DO
     */
    public ChatHistoryDO getConversationByConversationId(String conversationId, Long userId) {
        LambdaQueryWrapper<ChatHistoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistoryDO::getConversationId, conversationId)
                .eq(ChatHistoryDO::getUserId, userId);

        return chatHistoryMapper.selectOne(wrapper);
    }

    /**
     * 删除会话（通过主键ID）
     *
     * @param historyId 会话ID
     * @param userId 用户ID（用于权限检查）
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(Long historyId, Long userId) {
        // 先检查权限
        ChatHistoryDO history = chatHistoryMapper.selectById(historyId);
        if (history == null || !history.getUserId().equals(userId)) {
            log.warn("Unauthorized delete attempt: historyId={}, userId={}", historyId, userId);
            return false;
        }

        // 软删除
        chatHistoryMapper.deleteById(historyId);

        log.info("Deleted conversation: historyId={}, userId={}", historyId, userId);
        return true;
    }

    /**
     * 删除会话（通过 conversationId）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID（用于权限检查）
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversationByConversationId(String conversationId, Long userId) {
        // 先查找会话
        ChatHistoryDO history = getConversationByConversationId(conversationId, userId);
        if (history == null) {
            log.warn("Conversation not found or unauthorized: conversationId={}, userId={}", conversationId, userId);
            return false;
        }

        // 软删除
        chatHistoryMapper.deleteById(history.getId());

        log.info("Deleted conversation by conversationId: conversationId={}, historyId={}, userId={}", conversationId, history.getId(), userId);
        return true;
    }

    /**
     * 自动生成标题（基于第一条消息）
     *
     * @param historyId 会话ID
     * @param firstMessage 第一条消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateTitleFromMessage(Long historyId, String firstMessage) {
        // 截取前50个字符作为标题
        String title = firstMessage.length() > 50
                ? firstMessage.substring(0, 50) + "..."
                : firstMessage;

        updateTitle(historyId, title, "auto");
    }
}
