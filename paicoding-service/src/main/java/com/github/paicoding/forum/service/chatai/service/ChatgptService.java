package com.github.paicoding.forum.service.chatai.service;

/**
 * @author YiHui
 * @date 2023/6/2
 */
public interface ChatgptService {

    /**
     * 判断是否在会话中
     *
     * @param wxUuid
     * @return
     */
    boolean inChat(String wxUuid, String content);

    /**
     * 开始进入聊天
     *
     * @param content 输入的内容
     * @return chatgpt返回的结果
     */
    String chat(String wxUuid, String content);

}
