package com.github.paicoding.forum.web.front.chat.helper;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
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

    // fixme ai的切换，交给用户来选择
    AISourceEnum source = AISourceEnum.XUN_FEI_AI;

    public void sendMsgToUser(String session, String question) {
        ChatRecordsVo res = chatFacade.autoChat(source, question, vo -> response(session, vo));
        log.info("AI直接返回：{}", res);
    }

    public void sendMsgHistoryToUser(String session) {
        ChatRecordsVo vo = chatFacade.history(source);
        response(session, vo);
    }

    /**
     * 将返回结果推送给用户
     *
     * @param session
     * @param response
     */
    public void response(String session, ChatRecordsVo response) {
        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", response);
    }
}
