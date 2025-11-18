package com.github.paicoding.forum.service.chatv2.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;

/**
 * LLM 对话消息表
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessageDO extends BaseDO {

    /**
     * 会话ID (chat_history.id)
     */
    private Long historyId;

    /**
     * 角色: user/assistant/system/tool
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 输入 token 数（仅 assistant 消息有效）
     */
    private Integer promptTokens;

    /**
     * 输出 token 数（仅 assistant 消息有效）
     */
    private Integer completionTokens;

    /**
     * 总 token 数（仅 assistant 消息有效）
     */
    private Integer totalTokens;

    /**
     * 元数据 (JSON格式)
     */
    @TableField(value = "metadata_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadataJson;

    /**
     * 消息序号
     */
    private Integer sequenceNum;

    /**
     * 消息时间戳
     */
    private Date timestamp;

    /**
     * 删除标记: 0-正常 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
