package com.github.paicoding.forum.service.chatai.service.impl.xunfei;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.WsConnectStateEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * 讯飞星火大模型
 * <a href="https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E"/>
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class XunFeiAiServiceImpl extends AbsChatService {

    @Autowired
    private XunFeiIntegration xunFeiIntegration;

    /**
     * 不支持同步提问
     *
     * @param user
     * @param chat
     * @return
     */
    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        return AiChatStatEnum.IGNORE;
    }

    /**
     * 异步回答提问
     *
     * @param user
     * @param chatRes  保存提问 & 返回的结果，最终会返回给前端用户
     * @param consumer 具体将 response 写回前端的实现策略
     */
    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        XunFeiChatWrapper chat = new XunFeiChatWrapper(String.valueOf(user), chatRes, consumer);
        chat.initAndQuestion();
        return AiChatStatEnum.IGNORE;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.XUN_FEI_AI;
    }

    /**
     * 一个简单的ws装饰器，用于包装一下讯飞长连接的交互情况
     * 比较蛋疼的是讯飞建立连接60s没有返回主动断开，问了一次返回结果之后也主动断开，下次需要重连
     */
    @Data
    public class XunFeiChatWrapper {
        private OkHttpClient client;
        private WebSocket webSocket;
        private Request request;

        private BiConsumer<WebSocket, String> onMsg;

        private XunFeiMsgListener listener;

        private ChatItemVo item;

        public XunFeiChatWrapper(String uid, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
            client = xunFeiIntegration.getOkHttpClient();
            String url = xunFeiIntegration.buildXunFeiUrl();
            request = new Request.Builder().url(url).build();
            listener = new XunFeiMsgListener(uid, chatRes, consumer);
        }

        /**
         * 首次使用时，开启提问
         */
        public void initAndQuestion() {
            webSocket = client.newWebSocket(request, listener);
        }

        /**
         * 追加的提问, 主要是为了复用websocket的构造参数
         */
        public void appendQuestion(String uid, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
            listener = new XunFeiMsgListener(uid, chatRes, consumer);
            webSocket = client.newWebSocket(request, listener);
        }

    }

    @Getter
    @Setter
    public class XunFeiMsgListener extends WebSocketListener {
        private volatile WsConnectStateEnum connectState;

        private String user;

        private ChatRecordsVo chatRecord;

        private BiConsumer<AiChatStatEnum, ChatRecordsVo> callback;

        public XunFeiMsgListener(String user, ChatRecordsVo chatRecord, BiConsumer<AiChatStatEnum, ChatRecordsVo> callback) {
            this.connectState = WsConnectStateEnum.INIT;
            this.user = user;
            this.chatRecord = chatRecord;
            this.callback = callback;
        }

        //重写onopen
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            connectState = WsConnectStateEnum.CONNECTED;
            // 连接成功之后，发送消息
            webSocket.send(xunFeiIntegration.buildSendMsg(user, chatRecord.getRecords().get(0).getQuestion()));
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            ChatItemVo item = chatRecord.getRecords().get(0);
            XunFeiIntegration.ResponseData responseData = xunFeiIntegration.parse2response(text);
            if (responseData.successReturn()) {
                // 成功获取到结果
                StringBuilder msg = new StringBuilder();
                XunFeiIntegration.Payload pl = responseData.getPayload();
                pl.getChoices().getText().forEach(s -> {
                    msg.append(s.getContent());
                });
                item.appendAnswer(msg.toString());

                if (responseData.firstResonse()) {
                    callback.accept(AiChatStatEnum.FIRST, chatRecord);
                } else if (responseData.endResponse()) {
                    // 标记流式回答已完成
                    item.setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                    // 最后一次返回结果时，打印一下剩余的tokens
                    XunFeiIntegration.UsageText tokens = pl.getUsage().getText();
                    log.info("使用tokens:\n" + tokens);
                    webSocket.close(1001, "会话结束");
                    callback.accept(AiChatStatEnum.END, chatRecord);
                } else {
                    callback.accept(AiChatStatEnum.MID, chatRecord);
                }
            } else {
                item.initAnswer("AI返回异常:" + responseData.getHeader());
                callback.accept(AiChatStatEnum.ERROR, chatRecord);
                webSocket.close(responseData.getHeader().getCode(), responseData.getHeader().getMessage());
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            log.warn("websocket 连接失败! {}", response, t);
            connectState = WsConnectStateEnum.FAILED;
            chatRecord.getRecords().get(0).initAnswer("讯飞AI连接失败了!" + t.getMessage());
            callback.accept(AiChatStatEnum.ERROR, chatRecord);
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            if (log.isDebugEnabled()) {
                log.debug("连接中断! code={}, reason={}", code, reason);
            }
            connectState = WsConnectStateEnum.CLOSED;
        }
    }
}
