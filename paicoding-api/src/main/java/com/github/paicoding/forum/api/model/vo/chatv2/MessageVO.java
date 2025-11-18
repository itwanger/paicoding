package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 消息视图对象
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
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
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 消息序号
     */
    private Integer sequenceNum;

    /**
     * 消息时间戳
     */
    private Date timestamp;
}
