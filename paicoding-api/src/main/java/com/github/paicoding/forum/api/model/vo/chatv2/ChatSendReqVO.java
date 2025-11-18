package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

/**
 * 发送聊天消息请求
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
public class ChatSendReqVO {

    /**
     * 会话ID（UUID字符串，前端生成）
     */
    private String conversationId;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 用户消息
     */
    private String message;
}
