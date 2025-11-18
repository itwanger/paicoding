package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 会话视图对象
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
public class ConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话ID (UUID)
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 标题生成方式
     */
    private String titleGeneratedBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 消息列表（可选）
     */
    private List<MessageVO> messages;
}
