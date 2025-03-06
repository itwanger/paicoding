package com.github.paicoding.forum.web.front.chat.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatSessionItemVo;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.ws.WebSocketResponseUtil;
import com.github.paicoding.forum.service.chatai.service.ChatHistoryService;
import com.github.paicoding.forum.web.front.chat.helper.WsAnswerHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * STOMP协议的ChatGpt聊天通讯实现方式
 *
 * @author YiHui
 * @date 2023/6/5
 */
@Slf4j
@RestController
public class ChatRestController {
    @Autowired
    private WsAnswerHelper answerHelper;
    @Autowired
    private ChatHistoryService chatHistoryService;

    /**
     * 接收用户发送的消息
     *
     * @param msg
     * @param session
     * @param attrs
     * @return
     * @MessageMapping（"/chat/{session}"）注解的方法将用来接收"/app/chat/xxx路径发送来的消息，<br/> 如果有 @SendTo，则表示将返回结果，转发到其对应的路径上 （这个sendTo的路径，就是前端订阅的路径）
     * @DestinationVariable： 实现路径上的参数解析
     * @Headers 实现请求头格式的参数解析, @Header("headName") 表示获取某个请求头的内容
     */
    @MessageMapping("/chat/{session}")
    public void chat(String msg,
                     @DestinationVariable("session") String session,
                     @Header("simpSessionAttributes") Map<String, Object> attrs,
                     SimpMessageHeaderAccessor accessor) {
        String aiType = (String) attrs.get(WsAnswerHelper.AI_SOURCE_PARAM);
        WebSocketResponseUtil.execute(accessor, () -> {
            log.info("{} 用户开始了对话: {} - {}", ReqInfoContext.getReqInfo().getUser(), aiType, msg);
            AISourceEnum source = aiType == null ? null : AISourceEnum.valueOf(aiType);
            answerHelper.sendMsgToUser(source, session, msg);
        });
    }

    @MessageMapping({"/chat/{session}/{chatId}"})
    public void chat(String msg,
                     @DestinationVariable("session") String session,
                     @DestinationVariable("chatId") String chatId,
                     @Header("simpSessionAttributes") Map<String, Object> attrs,
                     SimpMessageHeaderAccessor accessor) {
        String aiType = (String) attrs.get(WsAnswerHelper.AI_SOURCE_PARAM);
        WebSocketResponseUtil.execute(accessor, () -> {
            // 设置会话id
            ReqInfoContext.getReqInfo().setChatId(chatId);
            log.info("{} 用户开始了对话: {} - {}", ReqInfoContext.getReqInfo().getUser(), aiType, msg);
            AISourceEnum source = aiType == null ? null : AISourceEnum.valueOf(aiType);
            answerHelper.sendMsgToUser(source, session, msg);
        });
    }

    /**
     * 查询用户的对话记录
     *
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "/chat/api/listSession")
    public ResVo<List<ChatSessionItemVo>> listChatSessions(String aiType) {
        AISourceEnum source = aiType == null ? null : AISourceEnum.valueOf(aiType);
        if (source == null) {
            return ResVo.ok(Collections.emptyList());
        }

        return ResVo.ok(chatHistoryService.listChatSessions(source, ReqInfoContext.getReqInfo().getUserId()));
    }


    /**
     * 返回用户的历史对话记录
     *
     * @param aiType
     * @param chatId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "/chat/api/syncHistory")
    public ResVo<Boolean> syncChatSessionHistory(String aiType, String chatId) {
        AISourceEnum source = aiType == null ? null : AISourceEnum.valueOf(aiType);
        if (source == null) {
            return ResVo.ok(false);
        }

        ReqInfoContext.getReqInfo().setChatId(chatId);
        SpringUtil.getBean(WsAnswerHelper.class).sendMsgHistoryToUser(ReqInfoContext.getReqInfo().getSession(), source);
        return ResVo.ok(true);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "/chat/api/updateSession")
    public ResVo<Boolean> updateChatSession(
            @RequestParam String aiType,
            @RequestParam String chatId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "deleted", required = false) Boolean deleted) {
        AISourceEnum source = aiType == null ? null : AISourceEnum.valueOf(aiType);
        if (source == null) {
            return ResVo.ok(false);
        }

        if (BooleanUtils.isTrue(deleted)) {
            return ResVo.ok(chatHistoryService.removeChatSession(source, chatId, ReqInfoContext.getReqInfo().getUserId()));
        } else {
            return ResVo.ok(chatHistoryService.updateChatSessionName(source, chatId, title, ReqInfoContext.getReqInfo().getUserId()));
        }
    }
}
