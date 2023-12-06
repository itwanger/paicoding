package com.github.paicoding.forum.service.notify.service;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * 私信聊天服务
 *
 * @author YiHui
 * @date 2023/12/6
 */
public interface NotifyChatService {
    /**
     * 用户的通知主题
     */
    String NOTICE_TOPIC = "/msg";

    /**
     * 私信聊天的消息类型
     */
    interface NotifyChatMsgType {
        // 系统消息: 加入群组
        int SYSTEM_ADD_CHAT = 0;
        // 系统消息: 在线人数
        int SYSTEM_CHAT_ONLINE = 1;

        /**
         * 用户消息
         */
        int USER_MSG = 100;
    }

    /**
     * 给用户发送通知
     *
     * @param userId
     * @param msg
     */
    void notifyToUser(Long userId, String msg);

    /**
     * 私信聊天的包装功能
     *
     * @param accessor
     */
    void chatWrapper(StompHeaderAccessor accessor);

    /**
     * 获取私聊信息
     *
     * @param channelId
     * @return
     */
    String getTmpChatChannelInfo(String channelId);
}
