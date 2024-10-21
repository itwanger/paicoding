package com.github.paicoding.forum.web.controller.chat.rest;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatSocketStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础的websocket实现通讯的方式
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class SimpleChatgptHandler extends TextWebSocketHandler {

    // 返回 TextMessage
    private TextMessage getTextMessage(String msg, Integer type) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", msg);
        map.put("type", type.toString());
        map.put("time", LocalDateTimeUtil.formatNormal(LocalDateTime.now()));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(map);

        return new TextMessage(json);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(getTextMessage("开始你和派聪明的AI之旅吧", ChatSocketStateEnum.Established.getCode()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 延迟 10 秒
        Thread.sleep(1000);
        TextMessage msg = getTextMessage(message.getPayload(), ChatSocketStateEnum.Payload.getCode());
        log.info("返回的内容是! {} = {}", ReqInfoContext.getReqInfo().getUserId(), msg);
        session.sendMessage(msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        session.sendMessage(getTextMessage("下次再撩吧（笑）", ChatSocketStateEnum.Closed.getCode()));
    }
}
