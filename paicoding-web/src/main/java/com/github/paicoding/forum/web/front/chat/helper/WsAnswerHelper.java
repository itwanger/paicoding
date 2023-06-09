package com.github.paicoding.forum.web.front.chat.helper;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatgpt.ChatFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * @author YiHui
 * @date 2023/6/9
 */
@Slf4j
@Component
public class WsAnswerHelper {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatFacade chatFacade;

    public void sendMsgToUser(String session, String question) {
        ChatRecordsVo vo = chatFacade.chat(AISourceEnum.PAI_AI, question);

        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", vo);
        log.info("结果已返回!");
    }

    public void sendMsgToUser(String session, ChatItemVo chat) {
        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", chat);
        log.info("结果已返回!");
    }


    public void sendMsgHistoryToUser(String session) {
        ChatRecordsVo vo = chatFacade.history(AISourceEnum.PAI_AI);
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", vo);
        log.info("聊天历史已经返回");
    }
}
