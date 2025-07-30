package com.github.paicoding.forum.service.chatai.service.impl.deepseek;

import cn.hutool.http.ContentType;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

    private OkHttpClient okHttpClient;

    @PostConstruct
    public void init() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)   // 建立连接的超时时间
                .readTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)  // 建立连接后读取数据的超时时间
                .writeTimeout(deepSeekConf.getTimeout(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * 一次性返回的交互方式
     * todo 待实现； 目前技术派主推流式交互，暂无下面的应用场景，留待有缘人补全
     */
    public boolean directReturn(ChatItemVo item) {
        return false;
    }

    /**
     * todo: 如果希望进行多轮对话，或者对上一次的对话进行补全，则可以在这里进行扩展，将历史的聊天记录传递给机器人，以获取更好的结果
     * <p>
     * 流式的操作交互方式
     *
     * @param item     聊天项目，包含了用户的问题或消息
     * @param listener 事件源监听器，用于处理流式返回的数据
     */
    public void streamReturn(ChatItemVo item, EventSourceListener listener) {
        // 创建一个新的聊天消息对象，设置角色为用户，并填充用户的问题
        List<ChatMsg> msg = toMsg(item);
        // 执行流式聊天，传入包含用户消息的消息列表和监听器
        this.executeStreamChat(msg, listener);
    }

    /**
     * 多轮对话的场景，将历史聊天记录，传递给聊天机器人，以获取更好的结果
     *
     * @param list     包含历史聊天记录的列表，用于构建对话上下文
     * @param listener 事件源监听器，用于处理聊天机器人的响应事件
     */
    public void streamReturn(List<ChatItemVo> list, EventSourceListener listener) {
        // 构建多轮聊天的会话上下文
        List<ChatMsg> msgList = ChatConstants.toMsgList(list, this::toMsg);
        // 执行流式聊天，将构建好的对话上下文传递给聊天机器人，并监听响应事件
        this.executeStreamChat(msgList, listener);
    }


    /**
     * 使用流式聊天接口发送聊天请求
     * 该方法将聊天请求转换为流式请求，并使用EventSource监听器处理响应
     *
     * @param req      聊天请求对象，包含聊天所需的参数
     * @param listener EventSource监听器，用于处理服务器发送的事件
     */
    private void executeStreamChat(ChatReq req, EventSourceListener listener) {
        // 设置请求为流式请求
        req.setStream(true);

        try {
            // 创建EventSource工厂，用于生成EventSource对象
            EventSource.Factory factory = EventSources.createFactory(okHttpClient);

            // 将聊天请求对象转换为JSON字符串
            String body = JsonUtil.toStr(req);
            // 构建请求对象，指定URL、认证头、内容类型头以及请求体
            Request request = new Request.Builder()
                    .url(deepSeekConf.getApiHost() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + deepSeekConf.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), body))
                    .build();
            // 使用工厂创建新的EventSource，并传入请求和监听器
            factory.newEventSource(request, listener);
        } catch (Exception e) {
            // 记录请求失败的日志
            log.error("deepseek联调请求失败: {}", req, e);
        }
    }

    private void executeStreamChat(List<ChatMsg> list, EventSourceListener listener) {
        ChatReq req = new ChatReq();
        req.setModel("deepseek-chat");
        req.setMessages(list);
        this.executeStreamChat(req, listener);
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "deepseek")
    private class DeepSeekConf {
        private String apiKey;
        private String apiHost;
        private Long timeout;
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
    @NoArgsConstructor
    @AllArgsConstructor
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

    private List<ChatMsg> toMsg(ChatItemVo item) {
        List<ChatMsg> list = new ArrayList<>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            // 提示词
            list.add(new ChatMsg("system", item.getQuestion().substring(ChatConstants.PROMPT_TAG.length())));
        } else {
            // 用户问答
            list.add(new ChatMsg("user", item.getQuestion()));
            if (StringUtils.isNotBlank(item.getAnswer())) {
                list.add(new ChatMsg("assistant", item.getAnswer()));
            }
        }
        return list;
    }
}
