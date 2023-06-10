package com.github.paicoding.forum.service.chatgpt.service;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;

import java.util.function.Consumer;

/**
 * @author YiHui
 * @date 2023/6/9
 */
public interface ChatService {

    /**
     * 具体AI选择
     *
     * @return
     */
    AISourceEnum source();

    /**
     * 开始进入聊天
     *
     * @param user     提问人
     * @param question 聊天的问题
     * @return chatgpt返回的结果
     */
    ChatRecordsVo chat(String user, String question);

    /**
     * 异步聊天
     *
     * @param user
     * @param question
     * @param consumer 执行成功之后，直接异步回调的通知
     * @return 同步直接返回的结果
     */
    ChatRecordsVo asyncChat(String user, String question, Consumer<ChatRecordsVo> consumer);


    /**
     * 查询聊天历史
     *
     * @param user
     * @return
     */
    ChatRecordsVo getChatHistory(String user);

}
