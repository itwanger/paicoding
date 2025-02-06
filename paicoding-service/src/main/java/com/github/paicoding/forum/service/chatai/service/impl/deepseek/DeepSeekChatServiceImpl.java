package com.github.paicoding.forum.service.chatai.service.impl.deepseek;

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
 * deepSeek 聊天接入
 *
 * @author YiHui
 * @date 2025/2/6
 */
@Slf4j
@Service
public class DeepSeekChatServiceImpl extends AbsChatService {
    @Autowired
    private DeepSeekIntegration deepSeekIntegration;

    /**
     * 同步的响应返回结果
     *
     * @param user
     * @param chat
     * @return
     */
    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        return null;
    }

    /**
     * 异步流式的返回结果
     *
     * @param user
     * @param response 保存提问 & 返回的结果，最终会返回给前端用户
     * @param consumer 具体将 response 写回前端的实现策略
     * @return
     */
    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo response, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        ChatItemVo item = response.getRecords().get(0);
        AbstractStreamListener listener = new AbstractStreamListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                super.onOpen(eventSource, response);
                if (log.isDebugEnabled()) {
                    log.debug("正确建立了连接: {}, res: {}", eventSource, response);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                if (log.isDebugEnabled()) {
                    log.debug("已经关闭了连接: {}", eventSource);
                }
                if (item.getAnswerType() != ChatAnswerTypeEnum.STREAM_END) {
                    // 主动结束这一次的对话
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
                // 成功返回结果的场景
                item.appendAnswer(message);
                consumer.accept(AiChatStatEnum.MID, response);
                if (log.isDebugEnabled()) {
                    log.debug("DeepSeek返回内容: {}", lastMessage);
                }
            }

            @Override
            public void onError(Throwable throwable, String res) {
                // 返回异常的场景
                item.appendAnswer("Error:" + (StringUtils.isBlank(res) ? throwable.getMessage() : res))
                        .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                consumer.accept(AiChatStatEnum.ERROR, response);
                if (log.isDebugEnabled()) {
                    log.debug("DeepSeek返回异常: {}", lastMessage);
                }
            }
        };

        // 注册回答结束的回调钩子
        listener.setOnComplate((s) -> {
            log.info("这一轮对话聊天已结束，完整的返回结果是：{}", s);
            item.appendAnswer("\n")
                    .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
            consumer.accept(AiChatStatEnum.END, response);
        });
        deepSeekIntegration.streamReturn(item, listener);
        return AiChatStatEnum.IGNORE;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.DEEP_SEEK;
    }
}
