package com.github.paicoding.forum.service.chatai.service.impl.ali;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(2)
class AliIntegrationTest {

    @Test
    void partialAnswerErrorShouldBecomeStreamEnd() {
        ChatRecordsVo records = recordsWithPartialAnswer();
        List<AiChatStatEnum> states = new ArrayList<>();
        AliIntegration integration = integration((param, callback) ->
                callback.onError(new IllegalStateException("stream interrupted")));

        integration.streamReturn(1L, records, (state, result) -> states.add(state));

        assertThat(states).containsExactly(AiChatStatEnum.ERROR);
        assertFailure(records.getRecords().get(0));
    }

    @Test
    void startupExceptionShouldCallbackError() {
        ChatRecordsVo records = recordsWithPartialAnswer();
        List<AiChatStatEnum> states = new ArrayList<>();
        AliIntegration integration = integration((param, callback) -> {
            throw new NoApiKeyException();
        });

        integration.streamReturn(1L, records, (state, result) -> states.add(state));

        assertThat(states).containsExactly(AiChatStatEnum.ERROR);
        assertFailure(records.getRecords().get(0));
    }

    @Test
    void errorThenCompleteShouldOnlyDeliverOneTerminal() {
        ChatRecordsVo records = recordsWithPartialAnswer();
        List<AiChatStatEnum> states = new ArrayList<>();
        AliIntegration integration = integration((param, callback) -> {
            callback.onError(new IllegalStateException("stream interrupted"));
            callback.onComplete();
        });

        integration.streamReturn(1L, records, (state, result) -> states.add(state));

        assertThat(states).containsExactly(AiChatStatEnum.ERROR);
        assertFailure(records.getRecords().get(0));
    }

    private AliIntegration integration(AliIntegration.GenerationClient client) {
        AliIntegration integration = new AliIntegration() {
            @Override
            GenerationClient newGenerationClient() {
                return client;
            }
        };
        AliIntegration.AliConfig config = new AliIntegration.AliConfig();
        config.setModel("qwen-test");
        ReflectionTestUtils.setField(integration, "config", config);
        return integration;
    }

    private ChatRecordsVo recordsWithPartialAnswer() {
        ChatItemVo item = new ChatItemVo().initQuestion("测试问题");
        item.appendAnswer("partial answer");
        assertThat(item.getAnswerType()).isEqualTo(ChatAnswerTypeEnum.STREAM);
        return new ChatRecordsVo().setRecords(Collections.singletonList(item));
    }

    private void assertFailure(ChatItemVo item) {
        assertThat(item.getAnswerType()).isEqualTo(ChatAnswerTypeEnum.STREAM_END);
        assertThat(item.getAnswer()).isEqualTo("AI 回复生成失败，请稍后再试");
    }
}
