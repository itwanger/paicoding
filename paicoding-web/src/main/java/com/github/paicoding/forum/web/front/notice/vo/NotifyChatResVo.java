package com.github.paicoding.forum.web.front.notice.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 聊天的返回信息
 *
 * @author YiHui
 * @date 2023/12/5
 */
@Data
@Accessors(chain = true)
public class NotifyChatResVo implements Serializable {
    private static final long serialVersionUID = 4487138961031301913L;

    /**
     * 发言人
     */
    private Long userId;
    /**
     * 发言人
     */
    private String userName;
    /**
     * 发言人头像
     */
    private String avatar;

    /**
     * 发言内容
     */
    private String content;
    /**
     * 消息时间
     */
    private Long date;

    /**
     * 消息类型：0系统消息 1用户消息
     */
    private int msgType;
}
