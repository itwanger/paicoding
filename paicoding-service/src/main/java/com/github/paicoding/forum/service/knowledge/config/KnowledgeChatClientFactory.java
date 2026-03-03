package com.github.paicoding.forum.service.knowledge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KnowledgeChatClientFactory {

    private final KnowledgeAiProperties properties;

    public ChatClient createClient() {
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .build();

        ChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModelName())
                        .maxTokens(properties.getMaxTokens())
                        .temperature(properties.getTemperature())
                        .build())
                .build();

        return ChatClient.builder(model).build();
    }
}
