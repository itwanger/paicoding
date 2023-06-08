package com.github.paicoding.forum.web.config;

import com.github.paicoding.forum.web.front.chat.rest.AuthHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * v1.1 stomp协议的websocket实现的chatgpt聊天方式
 *
 * @author YiHui
 * @date 2023/6/5
 */
@Configuration
@EnableWebSocketMessageBroker
public class WsChatConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 开启一个简单的基于内存的消息代理
        // 将消息返回到订阅了带 /chat 前缀的目的客户端
        config.enableSimpleBroker("/chat");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个 /websocket/{id} 的 WebSocket 终端
        // {id} 用于让用户连接终端时都可以有自己的路径
        // 作为 Principal 的标识，以便实现向指定用户发送信息
        registry.addEndpoint("/gpt/{id}")
                .setHandshakeHandler(new AuthHandshakeHandler());
    }
}
