package com.github.paicoding.forum.service.chatv2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * 流式聊天服务 - 处理 SSE 流式响应
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatService {

    private final ChatMemoryService chatMemoryService;
    private final ChatConversationService chatConversationService;
    private final TokenQuotaService tokenQuotaService;

    /**
     * 执行流式聊天请求
     *
     * @param chatClient ChatClient
     * @param historyId 会话ID
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param userMessage 用户消息
     * @return 流式响应
     */
    public Flux<String> executeStreamingChat(ChatClient chatClient, Long historyId, Long userId, String modelId, String userMessage) {
        AtomicBoolean streamCompleted = new AtomicBoolean(false);
        AtomicLong lastContentTime = new AtomicLong(System.currentTimeMillis());
        AtomicReference<ChatResponse> lastResponse = new AtomicReference<>();

        // 1. 更新会话时间
        chatConversationService.updateConversationTime(historyId);

        // 注意：用户消息和 AI 回复的保存由 MessageChatMemoryAdvisor 自动处理
        // 不需要手动调用 chatMemoryService.add()

        // 2. 创建心跳流（当长时间没有内容时发送心跳）
        Flux<String> heartbeatStream = Flux.interval(Duration.ofMillis(500))
                .takeWhile(i -> !streamCompleted.get())
                .flatMap(i -> {
                    long timeSinceLastContent = System.currentTimeMillis() - lastContentTime.get();
                    if (timeSinceLastContent > 800) {
                        return Flux.just("[HEARTBEAT]");
                    }
                    return Flux.empty();
                });

        // 3. 创建内容流
        // MessageChatMemoryAdvisor 会自动：
        //   - 通过 conversationId 参数从 ChatMemory 加载历史消息
        //   - 在请求完成后自动保存用户消息和 AI 回复
        String conversationIdStr = String.valueOf(historyId);
        log.info("Starting streaming chat for conversationId: {}, message: {}", conversationIdStr, userMessage);

        Flux<String> contentStream = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(CONVERSATION_ID, conversationIdStr))
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    lastContentTime.set(System.currentTimeMillis());
                    // 保存最后一个响应，用于获取 token 使用信息
                    // 在流式响应中，usage 信息通常在最后一个 chunk 中
                    if (response != null) {
                        lastResponse.set(response);
                        // 调试：检查每个响应是否有 usage 信息
                        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                            log.debug("Usage info found in chunk: {}", response.getMetadata().getUsage());
                        }
                    }
                })
                .flatMap(response -> {
                    if (response != null && response.getResult() != null
                        && response.getResult().getOutput() != null) {
                        String content = response.getResult().getOutput().getText();
                        if (content != null && !content.isEmpty()) {
                            log.debug("📤 Sending chunk: {}", content);
                            return Flux.just(content);
                        }
                    }
                    return Flux.empty();
                })
                .doOnComplete(() -> {
                    streamCompleted.set(true);
                    log.info("Streaming chat completed for historyId: {}", historyId);

                    // 提取 token 使用信息并更新配额
                    try {
                        extractAndRecordTokenUsage(lastResponse.get(), userId, modelId, historyId);
                    } catch (Exception e) {
                        log.error("Failed to record token usage for historyId: {}", historyId, e);
                    }
                })
                .doOnError(error -> {
                    streamCompleted.set(true);
                    log.error("Error during streaming chat for historyId: {}", historyId, error);
                });

        // 4. 合并心跳流和内容流，并添加完成标记
        return Flux.merge(heartbeatStream, contentStream)
                .concatWith(Flux.just("[DONE]"));
    }

    /**
     * 执行非流式聊天请求（用于测试）
     *
     * @param chatClient ChatClient
     * @param historyId 会话ID
     * @param userMessage 用户消息
     * @return 响应内容
     */
    public String executeChat(ChatClient chatClient, Long historyId, String userMessage) {
        // MessageChatMemoryAdvisor 会自动保存用户消息和 AI 回复
        // 执行请求
        String conversationIdStr = String.valueOf(historyId);
        log.info("Starting non-streaming chat for conversationId: {}", conversationIdStr);

        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(CONVERSATION_ID, conversationIdStr))
                .call()
                .content();

        // 更新会话时间
        chatConversationService.updateConversationTime(historyId);

        log.info("Non-streaming chat completed for historyId: {}", historyId);

        return response;
    }

    /**
     * 提取并记录 token 使用信息
     *
     * @param response ChatResponse
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param historyId 会话ID
     */
    private void extractAndRecordTokenUsage(ChatResponse response, Long userId, String modelId, Long historyId) {
        if (response == null) {
            log.warn("⚠️ ChatResponse is null, cannot extract token usage for historyId: {}", historyId);
            return;
        }

        try {
            // 从 ChatResponse 获取 metadata
            ChatResponseMetadata metadata = response.getMetadata();
            if (metadata == null) {
                log.warn("⚠️ ChatResponseMetadata is null for historyId: {}, response: {}", historyId, response);
                return;
            }

            // 获取 token 使用信息
            Usage usage = metadata.getUsage();
            if (usage == null) {
                log.warn("⚠️ Usage information is null for historyId: {}, metadata: {}", historyId, metadata);
                log.warn("⚠️ This may indicate the LLM API did not return usage info. Check if streamUsage=true is set.");
                return;
            }

            Integer promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens().intValue() : 0;
            Integer completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens().intValue() : 0;
            Integer totalTokens = usage.getTotalTokens() != null ? usage.getTotalTokens().intValue() : 0;

            log.info("✅ Token usage extracted: historyId={}, promptTokens={}, completionTokens={}, totalTokens={}",
                    historyId, promptTokens, completionTokens, totalTokens);

            // 如果 totalTokens 为 0，说明可能 API 没有返回正确的 usage 信息
            if (totalTokens == 0) {
                log.warn("⚠️ Total tokens is 0, skip quota deduction. This may indicate API compatibility issue.");
                return;
            }

            // 查找最新的 assistant 消息（由 MessageChatMemoryAdvisor 自动保存）
            // 注意：需要等待 advisor 保存完成，这里可能需要短暂延迟
            Long messageId = chatMemoryService.getLatestAssistantMessageId(historyId);
            if (messageId == null) {
                log.error("❌ Cannot find latest assistant message for historyId: {}. The advisor may not have saved the message yet.", historyId);
                // 尝试等待一下再重试
                try {
                    Thread.sleep(100);
                    messageId = chatMemoryService.getLatestAssistantMessageId(historyId);
                    if (messageId == null) {
                        log.error("❌ Still cannot find assistant message after retry for historyId: {}", historyId);
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting for assistant message", e);
                    return;
                }
            }

            log.info("📝 Found assistant messageId: {} for historyId: {}", messageId, historyId);

            // 扣除配额并记录 token 使用
            tokenQuotaService.deductQuotaAndRecord(
                    userId,
                    modelId,
                    messageId,
                    promptTokens,
                    completionTokens,
                    totalTokens
            );

            log.info("✅ Token quota deducted successfully: historyId={}, messageId={}, totalTokens={}",
                    historyId, messageId, totalTokens);

        } catch (Exception e) {
            log.error("❌ Failed to extract and record token usage for historyId: {}", historyId, e);
            // 不抛出异常，避免影响正常流程
        }
    }
}
