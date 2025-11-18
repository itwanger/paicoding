package com.github.paicoding.forum.service.chatv2.factory;

import com.github.paicoding.forum.service.chatv2.config.ChatV2ConfigProperties;
import com.github.paicoding.forum.service.chatv2.service.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ChatClient 工厂类
 * 启动时根据配置文件创建所有 ChatClient 和 ChatModel
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@Component
public class ChatClientFactory implements InitializingBean {

    private final ChatV2ConfigProperties chatV2Config;
    private final ChatMemoryService chatMemoryService;

    /**
     * 存储所有 ChatModel，key 为 modelId
     */
    private final Map<String, ChatModel> chatModels = new HashMap<>();

    /**
     * 存储所有 ChatClient，key 为 modelId
     */
    private final Map<String, ChatClient> chatClients = new HashMap<>();

    public ChatClientFactory(ChatV2ConfigProperties chatV2Config, ChatMemoryService chatMemoryService) {
        this.chatV2Config = chatV2Config;
        this.chatMemoryService = chatMemoryService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing ChatClient factory with {} model configurations",
                chatV2Config.getModels().size());

        // 只初始化启用的模型
        chatV2Config.getModels().stream()
                .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                .forEach(this::createChatClientForModel);

        log.info("Successfully initialized {} ChatClients: {}",
                chatClients.size(),
                chatClients.keySet().stream().collect(Collectors.joining(", ")));
    }

    /**
     * 为单个模型创建 ChatClient
     */
    private void createChatClientForModel(ChatV2ConfigProperties.ModelConfig config) {
        try {
            log.info("Creating ChatClient for model: {} ({})", config.getName(), config.getId());

            // 创建 OpenAI API 实例（兼容 OpenAI API 的其他模型）
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(config.getApiKey())
                    .baseUrl(config.getBaseUrl())
                    .build();

            // 创建 ChatModel
            ChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(config.getModelName())
                            .maxTokens(config.getMaxTokens())
                            .temperature(config.getTemperature())
                            .streamUsage(true)  // 启用流式响应中的 usage 信息
                            .build())
                    .build();

            // 创建 ChatClient 并添加 ChatMemory advisor
            // 参考 deepextract 实现：通过 MessageChatMemoryAdvisor 管理对话记忆
            // 注意：不设置 defaultConversationId，让每次调用时通过 advisorSpec.param() 动态指定
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(chatMemoryService)
                                    // 不设置 .defaultConversationId()，使用运行时传递的 conversationId
                                    .build()
                    )
                    .build();

            log.info("ChatClient for model {} configured with ChatMemory advisor (dynamic conversationId)", config.getName());

            // 存储
            chatModels.put(config.getId(), chatModel);
            chatClients.put(config.getId(), chatClient);

            log.info("Successfully created ChatClient for model: {} ({})",
                    config.getName(), config.getId());
        } catch (Exception e) {
            log.error("Failed to create ChatClient for model: {} ({})",
                    config.getName(), config.getId(), e);
            throw new RuntimeException("Failed to initialize ChatClient for model: " + config.getId(), e);
        }
    }

    /**
     * 获取指定模型的 ChatClient
     *
     * @param modelId 模型 ID
     * @return ChatClient
     */
    public ChatClient getChatClient(String modelId) {
        ChatClient chatClient = chatClients.get(modelId);
        if (chatClient == null) {
            throw new IllegalArgumentException("ChatClient not found for model: " + modelId);
        }
        return chatClient;
    }

    /**
     * 获取指定模型的 ChatModel
     *
     * @param modelId 模型 ID
     * @return ChatModel
     */
    public ChatModel getChatModel(String modelId) {
        ChatModel chatModel = chatModels.get(modelId);
        if (chatModel == null) {
            throw new IllegalArgumentException("ChatModel not found for model: " + modelId);
        }
        return chatModel;
    }

    /**
     * 获取默认 ChatClient
     *
     * @return ChatClient
     */
    public ChatClient getDefaultChatClient() {
        String defaultModelId = chatV2Config.getDefaultModel();
        return getChatClient(defaultModelId);
    }

    /**
     * 获取所有可用的模型 ID
     *
     * @return 模型 ID 列表
     */
    public Map<String, ChatClient> getAllChatClients() {
        return new HashMap<>(chatClients);
    }

    /**
     * 检查模型是否可用
     *
     * @param modelId 模型 ID
     * @return 是否可用
     */
    public boolean isModelAvailable(String modelId) {
        return chatClients.containsKey(modelId);
    }
}
