package com.github.paicoding.forum.service.chatgpt;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatgpt.service.ChatService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天的门面类
 *
 * @author YiHui
 * @date 2023/6/9
 */
@Service
public class ChatFacade {
    private final Map<AISourceEnum, ChatService> chatServiceMap;

    public ChatFacade(List<ChatService> chatServiceList) {
        chatServiceMap = new HashMap<>();
        for (ChatService chatService : chatServiceList) {
            chatServiceMap.put(chatService.source(), chatService);
        }
    }

    /**
     * 开始聊天
     *
     * @param question
     * @param source
     * @return
     */
    public ChatRecordsVo chat(AISourceEnum source, String question) {
        return chatServiceMap.get(source).chat(String.valueOf(ReqInfoContext.getReqInfo().getUserId()), question);
    }


    /**
     * 返回历史聊天记录
     *
     * @param source
     * @return
     */
    public ChatRecordsVo history(AISourceEnum source) {
        return chatServiceMap.get(source).getChatHistory(String.valueOf(ReqInfoContext.getReqInfo().getUserId()));
    }

}
