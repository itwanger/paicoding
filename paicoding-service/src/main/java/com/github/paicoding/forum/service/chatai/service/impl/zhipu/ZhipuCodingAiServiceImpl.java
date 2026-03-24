package com.github.paicoding.forum.service.chatai.service.impl.zhipu;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.plexpt.chatgpt.listener.AbstractStreamListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * 智谱 Coding Plan 聊天服务
 *
 * @author Codex
 * @date 2026/3/23
 */
@Slf4j
@Service
public class ZhipuCodingAiServiceImpl extends AbsChatService {
    @Autowired
    private ZhipuCodingIntegration zhipuCodingIntegration;

    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        if (zhipuCodingIntegration.directReturn(chat)) {
            return AiChatStatEnum.END;
        }
        return AiChatStatEnum.ERROR;
    }

    @Override
    public AiChatStatEnum doAnswer(Long user, ChatRecordsVo response) {
        if (zhipuCodingIntegration.directReturn(response.getRecords(), response.getRecords().get(0))) {
            return AiChatStatEnum.END;
        }
        return AiChatStatEnum.ERROR;
    }

    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo response, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        ChatItemVo item = response.getRecords().get(0);
        AbstractStreamListener listener = new AbstractStreamListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                super.onOpen(eventSource, response);
                if (log.isDebugEnabled()) {
                    log.debug("Zhipu Coding 连接建立成功: {}", eventSource);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                if (item.getAnswerType() != ChatAnswerTypeEnum.STREAM_END) {
                    if (StringUtils.isBlank(lastMessage)) {
                        item.appendAnswer("大模型超时未返回结果，主动关闭会话；请重新提问吧\n")
                                .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                        consumer.accept(AiChatStatEnum.ERROR, response);
                    } else {
                        item.appendAnswer("\n")
                                .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                        consumer.accept(AiChatStatEnum.END, response);
                    }
                }
            }

            @Override
            public void onMsg(String message) {
                if (StringUtils.isNotBlank(lastMessage)) {
                    item.appendAnswer(message);
                    consumer.accept(AiChatStatEnum.MID, response);
                }
            }

            @Override
            public void onError(Throwable throwable, String res) {
                item.appendAnswer("Error:" + (StringUtils.isBlank(res) ? throwable.getMessage() : res))
                        .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                consumer.accept(AiChatStatEnum.ERROR, response);
            }
        };

        listener.setOnComplate((s) -> {
            item.appendAnswer("\n").setAnswerType(ChatAnswerTypeEnum.STREAM_END);
            consumer.accept(AiChatStatEnum.END, response);
        });
        zhipuCodingIntegration.streamReturn(response.getRecords(), listener);
        return AiChatStatEnum.IGNORE;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.ZHIPU_CODING;
    }
}
