package com.github.paicoding.forum.service.chatai.service.impl.xunfei;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.WsConnectStateEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.google.common.base.Joiner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 讯飞星火大模型
 * <a href="https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E"/>
 *
 * @author YiHui
 * @date 2023/6/12
 */
@Slf4j
@Service
public class XunFeiAiServiceImpl extends AbsChatService {
    /**
     * 这里使用Caffeine的内存缓存，再使用层面上与Guava没有太大的差别
     */
    private Cache<String, XunFeiChatDecorate> cache;

    public XunFeiAiServiceImpl() {
        cache = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(100).build();
    }

    /**
     * 不支持同步提问
     *
     * @param user
     * @param chat
     * @return
     */
    @Override
    public boolean doAnswer(String user, ChatItemVo chat) {
        return false;
    }

    /**
     * 异步回答提问
     *
     * @param user
     * @param chatRes  保存提问 & 返回的结果，最终会返回给前端用户
     * @param consumer 具体将 response 写回前端的实现策略
     * @return
     */
    @Override
    public boolean doAsyncAnswer(String user, ChatRecordsVo chatRes, BiConsumer<Boolean, ChatRecordsVo> consumer) {
        XunFeiChatDecorate xunFeiChat = cache.get(user, key -> {
            XunFeiChatDecorate chat = new XunFeiChatDecorate() {
                /**
                 * 接收到讯飞返回的结果，然后将其封装之后返回给用户
                 * @param webSocket
                 * @param text
                 */
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    super.onMessage(webSocket, text);
                    ChatItemVo item = chatRes.getRecords().get(0);
                    XunFeiIntegration.ResponseData responseData = JsonUtil.toObj(text, XunFeiIntegration.ResponseData.class);
                    if (responseData.successReturn()) {
                        // 成功获取到结果
                        XunFeiIntegration.Payload pl = responseData.getPayload();
                        List<XunFeiIntegration.ChoicesText> txt = pl.getChoices().getText();
                        String ans = Joiner.on("\n").join(txt);
                        item.initAnswer(ans);

                        if (responseData.endResponse()) {
                            // 最后一次返回结果时，打印一下剩余的tokens
                            XunFeiIntegration.UsageText tokens = pl.getUsage().getText();
                            log.info("使用tokens:\n" + tokens);
                        }

                        // 结果响应
                        consumer.accept(true, chatRes);
                    } else {
                        item.initAnswer("AI返回异常:" + responseData.getHeader());
                        consumer.accept(false, chatRes);
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    chatRes.getRecords().get(0).initAnswer("讯飞AI启动失败:" + response.message());
                    consumer.accept(false, chatRes);
                }
            };
            chat.startConnect();
            return chat;
        });


        // 基于websocket的连接状态，做一些引导性的回复和业务等待
        int maxCnt = 100;
        while (true) {
            if (--maxCnt <= 0 || xunFeiChat == null) {
                chatRes.getRecords().get(0).initAnswer("讯飞AI启动失败，请稍后再试");
                consumer.accept(false, chatRes);
                break;
            }
            if (xunFeiChat.connectState == WsConnectStateEnum.CONNECTED) {
                xunFeiChat.getWebSocket().send(chatRes.getRecords().get(0).getQuestion());
                break;
            } else if (xunFeiChat.connectState == WsConnectStateEnum.CONNECTING || xunFeiChat.connectState == WsConnectStateEnum.INIT) {
                AsyncUtil.sleep(500);
            } else if (xunFeiChat.connectState == WsConnectStateEnum.CLOSED) {
                // websocket 已经关闭了，需要重新连接一下
                xunFeiChat.startConnect();
            } else if (xunFeiChat.connectState == WsConnectStateEnum.FAILED) {
                // 会话连接失败
                chatRes.getRecords().get(0).initAnswer("讯飞AI连接失败了，请稍后再试吧");
                consumer.accept(false, chatRes);
                break;
            }
        }
        return false;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.XUN_FEI_AI;
    }

    /**
     * 一个简单的ws装饰器，用于包装一下讯飞长连接的交互响应情况
     */
    @Data
    public static class XunFeiChatDecorate extends WebSocketListener {
        private volatile WsConnectStateEnum connectState = WsConnectStateEnum.INIT;


        private WebSocket webSocket;

        //重写onopen
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            connectState = WsConnectStateEnum.CONNECTED;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            log.error("websocket 连接失败! {}", response, t);
            connectState = WsConnectStateEnum.FAILED;
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            log.info("连接中断! code={}, reason={}", code, reason);
            connectState = WsConnectStateEnum.CLOSED;
        }


        public void startConnect() {
            this.setConnectState(WsConnectStateEnum.CONNECTING);
            this.webSocket = SpringUtil.getBean(XunFeiIntegration.class).newWebSocket(this);
        }
    }
}
