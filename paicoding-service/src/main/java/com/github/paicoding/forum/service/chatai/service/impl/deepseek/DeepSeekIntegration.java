package com.github.paicoding.forum.service.chatai.service.impl.deepseek;

import cn.hutool.http.ContentType;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek的集成类，主要负责与DeepSeek进行交互
 *
 * @author YiHui
 * @date 2025/2/6
 */
@Slf4j
@Component
public class DeepSeekIntegration {
    @Autowired
    private DeepSeekConf deepSeekConf;

    private DeepSeekChat chat;

    @PostConstruct
    public void init() {
        this.chat = new DeepSeekChat();
    }

    /**
     * todo: 如果希望进行多轮对话，或者对上一次的对话进行补全，则可以在这里进行扩展，将历史的聊天记录传递给机器人，以获取更好的结果
     * <p>
     * 流式的操作交互方式
     *
     * @param item
     * @param listener
     */
    public void streamReturn(ChatItemVo item, EventSourceListener listener) {
        ChatMsg msg = new ChatMsg();
        msg.setRole("user");
        msg.setContent(item.getQuestion());
        this.chat.streamChat(Arrays.asList(msg), listener);
    }


    /**
     * 一次性返回的交互方式
     * todo 待实现； 目前技术派主推流式交互，暂无下面的应用场景，留待有缘人补全
     */
    public void directReturn() {
    }


    @Data
    @Component
    @ConfigurationProperties(prefix = "deepseek")
    private class DeepSeekConf {
        private String apiKey;
        private String apiHost;
        private Long timeout;
    }

    public class DeepSeekChat {
        private OkHttpClient okHttpClient;


        private DeepSeekChat() {
            this.okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)   // 建立连接的超时时间
                    .readTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)  // 建立连接后读取数据的超时时间
                    .writeTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)
                    .build();
        }

        public void streamChat(ChatReq req, EventSourceListener listener) {
            req.setStream(true);

            try {
                EventSource.Factory factory = EventSources.createFactory(okHttpClient);

                String body = JsonUtil.toStr(req);
                Request request = new Request.Builder()
                        .url(deepSeekConf.getApiHost() + "/chat/completions")
                        .addHeader("Authorization", "Bearer " + deepSeekConf.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), body))
                        .build();
                factory.newEventSource(request, listener);
            } catch (Exception e) {
                log.error("deepseek联调请求失败: {}", req, e);
            }
        }

        public void streamChat(List<ChatMsg> list, EventSourceListener listener) {
            ChatReq req = new ChatReq();
            req.setModel("deepseek-chat");
            req.setMessages(list);
            this.streamChat(req, listener);
        }
    }


    /**
     * 提问的请求实体
     * todo: 这里只封装了最基础的请求传参，更多的参数可以根据官方文档进行补全
     * <a href="https://api-docs.deepseek.com/zh-cn/api/create-chat-completion"/>
     */
    @Data
    public static class ChatReq {
        /**
         * 模型，官方当前支持两个：
         * 1. deepseek-chat
         * 2. deepseek-reasoner --> 推理模型
         */
        private String model;

        /**
         * true 来使用流式输出
         */
        private boolean stream;

        /**
         * 对话内容
         */
        private List<ChatMsg> messages;
    }

    @Data
    public static class ChatMsg {
        /**
         * 角色：用于AI了解它应该如何行为以及谁在发起调用
         * - system: 了解它应该如何行为以及谁在发起调用， 如 content = 你现在是一个资深后端java工程师
         * - user:  消息/提示来自最终用户或人类
         * - assistant: 消息是助手（聊天模型）的响应 --> 即ai的返回结果，在多轮对话中，我们需要将之前的聊天记录传递给机器人，以获取更好的结果
         */
        private String role;

        /**
         * 具体的内容
         */
        private String content;
    }
}
