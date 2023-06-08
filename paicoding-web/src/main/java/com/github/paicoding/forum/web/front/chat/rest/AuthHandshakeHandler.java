package com.github.paicoding.forum.web.front.chat.rest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * @author YiHui
 * @date 2023/6/8
 */
public class AuthHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 获取例如 ws://localhost/gpt/1 订阅地址
        // 中的最后一个用户 id 参数作为用户的标识,
        // 为实现发送信息给指定用户做准备
        String uri = request.getURI().toString();
        String uid = uri.substring(uri.lastIndexOf("/") + 1);
        return () -> uid;
    }
}
