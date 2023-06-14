package com.github.paicoding.forum.web.front.chat.helper;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.ChatFacade;
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
        // 模拟问答： 50%的问答直接回复，50%的问答异步返回
//        ChatRecordsVo vo = Math.random() > 0.5f
//                ? chatFacade.chat(AISourceEnum.PAI_AI, question)
//                : chatFacade.asyncChat(AISourceEnum.PAI_AI, question, (res) -> {
//            simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", res);
//        });

        // 讯飞AI
//        ChatRecordsVo vo = chatFacade.asyncChat(AISourceEnum.XUN_FEI_AI, question, (res) -> {
//            simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", res);
//        });

        ChatRecordsVo vo = chatFacade.chat(AISourceEnum.CHAT_GPT_3_5, question);
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", vo);

        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志

        log.info("同步直接回复：{}", vo);
    }

    public void sendMsgHistoryToUser(String session) {
//        ChatRecordsVo vo = chatFacade.history(AISourceEnum.PAI_AI);

        ChatRecordsVo vo = chatFacade.history(AISourceEnum.CHAT_GPT_3_5);
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", vo);
    }
}
