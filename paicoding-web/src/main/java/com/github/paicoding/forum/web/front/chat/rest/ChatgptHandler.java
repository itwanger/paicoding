package com.github.paicoding.forum.web.front.chat.rest;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YiHui
 * @date 2023/6/5
 */
public class ChatgptHandler extends TextWebSocketHandler {

    // 返回 TextMessage
    private TextMessage getTextMessage(String msg) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", msg);
        map.put("time", LocalDateTimeUtil.formatNormal(LocalDateTime.now()));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(map);

        return new TextMessage(json);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(getTextMessage("开始你和派聪明的AI之旅吧"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 延迟 10 秒
        Thread.sleep(10000);
        session.sendMessage(getTextMessage(message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        session.sendMessage(getTextMessage("下次再撩吧（笑）"));
    }
}
