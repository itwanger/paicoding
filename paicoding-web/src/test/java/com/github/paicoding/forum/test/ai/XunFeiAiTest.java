package com.github.paicoding.forum.test.ai;

import com.github.paicoding.forum.api.model.enums.WsConnectStateEnum;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiAiServiceImpl;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiIntegration;
import okhttp3.WebSocket;
import org.junit.Test;

/**
 * @author YiHui
 * @date 2023/6/12
 */
public class XunFeiAiTest {

    @Test
    public void testAi() {
        String question = "说一个笑话";//可以修改question 内容，来向模型提问

        XunFeiIntegration xunFeiIntegration = new XunFeiIntegration();
        XunFeiAiServiceImpl.XunFeiChatDecorate listener = new XunFeiAiServiceImpl.XunFeiChatDecorate();
        WebSocket webSocket = xunFeiIntegration.newWebSocket(listener);
        AsyncUtil.sleep(10000);
        while (true) {
            if (listener.getConnectState() != WsConnectStateEnum.CONNECTED) {
                System.out.println("连接未建立! 稍等一会~");
                AsyncUtil.sleep(1000);
                continue;
            }
            String msg = xunFeiIntegration.buildSendMsg("123", question);
            break;
        }
        AsyncUtil.sleep(3600_000);
    }
}
