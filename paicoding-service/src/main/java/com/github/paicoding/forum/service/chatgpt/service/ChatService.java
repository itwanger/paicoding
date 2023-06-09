package com.github.paicoding.forum.service.chatgpt.service;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;

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
     * 查询聊天历史
     *
     * @param user
     * @return
     */
    ChatRecordsVo getChatHistory(String user);

}
