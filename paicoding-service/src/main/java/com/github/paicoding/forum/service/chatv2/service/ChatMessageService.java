package com.github.paicoding.forum.service.chatv2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.chatv2.repository.entity.ChatMessageDO;
import com.github.paicoding.forum.service.chatv2.repository.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    /**
     * 获取会话的所有消息
     *
     * @param historyId 会话ID
     * @return 消息列表
     */
    public List<ChatMessageDO> getMessagesByHistoryId(Long historyId) {
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId)
                .orderByAsc(ChatMessageDO::getSequenceNum);

        return chatMessageMapper.selectList(wrapper);
    }

    /**
     * 获取会话的最近N条消息
     *
     * @param historyId 会话ID
     * @param limit 数量限制
     * @return 消息列表
     */
    public List<ChatMessageDO> getRecentMessages(Long historyId, int limit) {
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId)
                .orderByDesc(ChatMessageDO::getSequenceNum)
                .last("LIMIT " + limit);

        List<ChatMessageDO> messages = chatMessageMapper.selectList(wrapper);

        // 反转顺序（因为查询是倒序的）
        java.util.Collections.reverse(messages);

        return messages;
    }

    /**
     * 统计会话的消息数量
     *
     * @param historyId 会话ID
     * @return 消息数量
     */
    public Long countMessages(Long historyId) {
        LambdaQueryWrapper<ChatMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageDO::getHistoryId, historyId);

        return chatMessageMapper.selectCount(wrapper);
    }
}
