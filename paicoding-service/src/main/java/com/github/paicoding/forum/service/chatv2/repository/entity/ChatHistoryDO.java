package com.github.paicoding.forum.service.chatv2.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM 对话会话表
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_history")
public class ChatHistoryDO extends BaseDO {

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
     * 标题生成方式: auto/user/llm
     */
    private String titleGeneratedBy;

    /**
     * 删除标记: 0-正常 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
