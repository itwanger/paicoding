package com.github.paicoding.forum.service.chatai.service.impl.deepseek;

import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.search.AiWebSearchService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final int MAX_TOOL_STEPS = 2;
    private static final String WEB_SEARCH_TOOL_NAME = "web_search";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Autowired
    private DeepSeekConf deepSeekConf;
    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;
    @Autowired
    private AiWebSearchService aiWebSearchService;

    private OkHttpClient okHttpClient;

    @PostConstruct
    public void init() {
        dynamicConfigContainer.registerRefreshCallback(deepSeekConf, this::refreshClient);
        refreshClient();
    }

    private void refreshClient() {
        long timeout = deepSeekConf.getTimeout() == null ? 900L : deepSeekConf.getTimeout();
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)   // 建立连接的超时时间
                .readTimeout(timeout, TimeUnit.SECONDS)  // 建立连接后读取数据的超时时间
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 一次性返回的交互方式
     */
    public boolean directReturn(ChatItemVo item) {
        return directReturn(java.util.Arrays.asList(item), item);
    }

    public boolean directReturn(List<ChatItemVo> items, ChatItemVo answerTarget) {
        List<ChatMsg> msg = ChatConstants.toMsgList(items, this::toMsg);
        ChatReq req = new ChatReq();
        req.setModel(getModel());
        req.setMessages(msg);
        req.setStream(false);
        if (aiWebSearchService.isAvailable()) {
            req.setTools(buildWebSearchTools());
            req.setToolChoice("auto");
        }
        
        try {
            String content = executeReActChat(req);
            if (StringUtils.isNotBlank(content)) {
                answerTarget.initAnswer(content);
                log.info("DeepSeek AI 调用成功! 传参:{}, 返回:{}", JsonUtil.toStr(msg), content);
                return true;
            }
            log.error("DeepSeek调用失败，未返回有效内容");
            return false;
        } catch (Exception e) {
            log.error("deepseek调用失败", e);
            return false;
        }
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

    public void streamReActReturn(List<ChatItemVo> list, EventSourceListener listener) {
        ChatReq req = new ChatReq();
        req.setModel(getModel());
        req.setMessages(ChatConstants.toMsgList(list, this::toMsg));
        req.setStream(false);
        req.setTools(buildWebSearchTools());
        req.setToolChoice("auto");
        try {
            ReActStreamPrepareResult result = prepareReActStream(req);
            if (result.isNeedFinalStream()) {
                req.setTools(null);
                req.setToolChoice(null);
                executeStreamChat(req, listener);
            } else {
                emitBufferedContent(listener, result.getFinalContent());
            }
        } catch (Exception e) {
            log.error("DeepSeek ReAct 流式调用失败", e);
            listener.onFailure(null, e, null);
        }
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
        req.setModel(getModel());
        req.setMessages(list);
        this.executeStreamChat(req, listener);
    }

    public boolean isWebSearchToolAvailable() {
        return aiWebSearchService.isAvailable();
    }

    private ReActStreamPrepareResult prepareReActStream(ChatReq req) throws Exception {
        JSONObject jsonObject = postChatCompletion(req);
        JSONArray choices = jsonObject.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("DeepSeek 未返回 choices");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new IllegalStateException("DeepSeek 未返回 message");
        }

        JSONArray toolCalls = message.getJSONArray("tool_calls");
        if (toolCalls == null || toolCalls.isEmpty()) {
            return ReActStreamPrepareResult.finalContent(message.getString("content"));
        }

        ChatMsg assistantMsg = toAssistantToolCallMessage(message);
        req.getMessages().add(assistantMsg);
        for (int j = 0; j < toolCalls.size(); j++) {
            ToolCall toolCall = parseToolCall(toolCalls.getJSONObject(j));
            req.getMessages().add(executeToolCall(toolCall));
        }
        return ReActStreamPrepareResult.needFinalStream();
    }

    private String executeReActChat(ChatReq req) throws Exception {
        for (int i = 0; i <= MAX_TOOL_STEPS; i++) {
            JSONObject jsonObject = postChatCompletion(req);
            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("DeepSeek 未返回 choices");
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null) {
                throw new IllegalStateException("DeepSeek 未返回 message");
            }

            JSONArray toolCalls = message.getJSONArray("tool_calls");
            if (toolCalls == null || toolCalls.isEmpty()) {
                return message.getString("content");
            }
            if (i >= MAX_TOOL_STEPS) {
                return StringUtils.defaultIfBlank(message.getString("content"), "DeepSeek 工具调用超过最大轮数，请稍后重试。");
            }

            ChatMsg assistantMsg = toAssistantToolCallMessage(message);
            req.getMessages().add(assistantMsg);
            for (int j = 0; j < toolCalls.size(); j++) {
                ToolCall toolCall = parseToolCall(toolCalls.getJSONObject(j));
                req.getMessages().add(executeToolCall(toolCall));
            }
        }
        return null;
    }

    private JSONObject postChatCompletion(ChatReq req) throws Exception {
        String body = JsonUtil.toStr(req);
        Request request = new Request.Builder()
                .url(deepSeekConf.getApiHost() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + deepSeekConf.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), body))
                .build();

        okhttp3.Response response = okHttpClient.newCall(request).execute();
        try {
            String responseBody = response.body() == null ? null : response.body().string();
            if (response.isSuccessful() && StringUtils.isNotBlank(responseBody)) {
                return com.alibaba.fastjson.JSON.parseObject(responseBody);
            }
            throw new IllegalStateException("DeepSeek 调用失败: code=" + response.code() + ", body=" + StringUtils.defaultString(responseBody));
        } finally {
            response.close();
        }
    }

    private ChatMsg executeToolCall(ToolCall toolCall) {
        String result;
        if (toolCall == null || toolCall.getFunction() == null || StringUtils.isBlank(toolCall.getFunction().getName())) {
            result = "工具调用参数缺失，无法执行。";
            return ChatMsg.tool(toolCall == null ? null : toolCall.getId(), result);
        }
        if (!WEB_SEARCH_TOOL_NAME.equals(toolCall.getFunction().getName())) {
            result = "不支持的工具：" + toolCall.getFunction().getName();
            return ChatMsg.tool(toolCall.getId(), result);
        }

        String query = extractSearchQuery(toolCall.getFunction().getArguments());
        log.info("DeepSeek 发起联网搜索, query={}", query);
        result = aiWebSearchService.search(query);
        return ChatMsg.tool(toolCall.getId(), result);
    }

    private ToolCall parseToolCall(JSONObject toolCallJson) {
        ToolCall toolCall = new ToolCall();
        toolCall.setId(toolCallJson.getString("id"));
        toolCall.setType(toolCallJson.getString("type"));
        JSONObject functionJson = toolCallJson.getJSONObject("function");
        if (functionJson != null) {
            FunctionCall functionCall = new FunctionCall();
            functionCall.setName(functionJson.getString("name"));
            functionCall.setArguments(functionJson.getString("arguments"));
            toolCall.setFunction(functionCall);
        }
        return toolCall;
    }

    private ChatMsg toAssistantToolCallMessage(JSONObject message) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setRole("assistant");
        chatMsg.setContent(message.getString("content"));
        List<ToolCall> toolCalls = new ArrayList<>();
        JSONArray array = message.getJSONArray("tool_calls");
        for (int i = 0; array != null && i < array.size(); i++) {
            toolCalls.add(parseToolCall(array.getJSONObject(i)));
        }
        chatMsg.setToolCalls(toolCalls);
        return chatMsg;
    }

    private String extractSearchQuery(String arguments) {
        if (StringUtils.isBlank(arguments)) {
            return "";
        }
        try {
            JSONObject args = com.alibaba.fastjson.JSON.parseObject(arguments);
            return StringUtils.defaultIfBlank(args.getString("query"), arguments);
        } catch (Exception e) {
            return arguments;
        }
    }

    private List<Tool> buildWebSearchTools() {
        Tool tool = new Tool();
        tool.setType("function");
        ToolFunction function = new ToolFunction();
        function.setName(WEB_SEARCH_TOOL_NAME);
        function.setDescription("联网搜索工具。仅当用户问题涉及现在、最新、截至、价格、版本、发布时间、数量、行业现状、市场采用情况，或参考资料可能过期时调用。");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        query.put("type", "string");
        query.put("description", "要搜索的关键词或问题，尽量简洁明确。");
        properties.put("query", query);
        parameters.put("properties", properties);
        parameters.put("required", Arrays.asList("query"));
        function.setParameters(parameters);
        tool.setFunction(function);
        return Arrays.asList(tool);
    }

    private void emitBufferedContent(EventSourceListener listener, String content) {
        if (StringUtils.isNotBlank(content)) {
            int chunkSize = 12;
            for (int i = 0; i < content.length(); i += chunkSize) {
                emitContent(listener, content.substring(i, Math.min(content.length(), i + chunkSize)));
            }
        }
        listener.onEvent(null, null, null, "[DONE]");
    }

    private void emitContent(EventSourceListener listener, String content) {
        JSONObject delta = new JSONObject();
        delta.put("content", content);
        JSONObject choice = new JSONObject();
        choice.put("delta", delta);
        JSONArray choices = new JSONArray();
        choices.add(choice);
        JSONObject event = new JSONObject();
        event.put("choices", choices);
        listener.onEvent(null, null, null, event.toJSONString());
    }

    private String getModel() {
        return StringUtils.defaultIfBlank(deepSeekConf.getModel(), DEFAULT_MODEL);
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "deepseek")
    public static class DeepSeekConf {
        private String apiKey;
        private String apiHost;
        private String model;
        private Long timeout;
    }


    /**
     * 提问的请求实体
     * todo: 这里只封装了最基础的请求传参，更多的参数可以根据官方文档进行补全
     * <a href="https://api-docs.deepseek.com/zh-cn/api/create-chat-completion"/>
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        /**
         * Tool calling definitions.
         */
        private List<Tool> tools;

        @JsonProperty("tool_choice")
        private String toolChoice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;

        @JsonProperty("tool_call_id")
        private String toolCallId;

        public ChatMsg(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static ChatMsg tool(String toolCallId, String content) {
            ChatMsg msg = new ChatMsg();
            msg.setRole("tool");
            msg.setToolCallId(toolCallId);
            msg.setContent(content);
            return msg;
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {
        private String type;
        private ToolFunction function;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolFunction {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolCall {
        private String id;
        private String type;
        private FunctionCall function;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FunctionCall {
        private String name;
        private String arguments;
    }

    @Data
    @AllArgsConstructor
    private static class ReActStreamPrepareResult {
        private boolean needFinalStream;
        private String finalContent;

        static ReActStreamPrepareResult needFinalStream() {
            return new ReActStreamPrepareResult(true, null);
        }

        static ReActStreamPrepareResult finalContent(String content) {
            return new ReActStreamPrepareResult(false, content);
        }
    }

    private List<ChatMsg> toMsg(ChatItemVo item) {
        List<ChatMsg> list = new ArrayList<>(2);
        if (item.getQuestion().startsWith(ChatConstants.PROMPT_TAG)) {
            // 提示词
            list.add(new ChatMsg("system", item.getQuestion().substring(ChatConstants.PROMPT_TAG.length()) + buildWebSearchPrompt()));
        } else {
            // 用户问答
            list.add(new ChatMsg("user", item.getQuestion()));
            if (StringUtils.isNotBlank(item.getAnswer())) {
                list.add(new ChatMsg("assistant", item.getAnswer()));
            }
        }
        return list;
    }

    private String buildWebSearchPrompt() {
        if (!aiWebSearchService.isAvailable()) {
            return "";
        }
        return "\n\n当前日期：" + LocalDate.now().format(DATE_FORMATTER)
                + "。当用户问题涉及现在、最新、截至、价格、版本、发布时间、数量、行业现状、市场采用情况，"
                + "或参考资料可能过期时，请使用 web_search 工具获取最新信息。最终回答不要暴露工具调用过程。";
    }
}
