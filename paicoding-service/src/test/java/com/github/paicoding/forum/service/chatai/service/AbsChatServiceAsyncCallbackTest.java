package com.github.paicoding.forum.service.chatai.service;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.impl.pai.PaiAiDemoServiceImpl;
import com.github.paicoding.forum.service.sensitive.service.SensitiveAiOptimizeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsChatServiceAsyncCallbackTest {

    @Mock
    private SensitiveAiOptimizeService sensitiveAiOptimizeService;
    @Mock
    private ChatHistoryService chatHistoryService;

    private FailingPostProcessService service;

    @BeforeEach
    void setUp() {
        service = new FailingPostProcessService();
        ReflectionTestUtils.setField(service, "sensitiveAiOptimizeService", sensitiveAiOptimizeService);
        ReflectionTestUtils.setField(service, "chatHistoryService", chatHistoryService);
        ReflectionTestUtils.setField(service, "chatHistoryContextNum", 10);

        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(42L);
        reqInfo.setChatId("comment:1_42");
        ReqInfoContext.addReqInfo(reqInfo);
    }

    @AfterEach
    void tearDown() {
        ReqInfoContext.clear();
    }

    @Test
    void postProcessingFailureShouldNotBlockTerminalConsumer() {
        when(sensitiveAiOptimizeService.sanitizeChatQuestion(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chatHistoryService.listHistory(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        AtomicReference<ChatRecordsVo> delivered = new AtomicReference<>();

        service.asyncChat(42L, "测试问题", delivered::set);

        ChatItemVo item = delivered.get().getRecords().get(0);
        assertThat(item.getAnswer()).isEqualTo("有效回答");
        assertThat(item.getAnswerType()).isEqualTo(ChatAnswerTypeEnum.STREAM_END);
    }

    @Test
    void paiAsyncFallbackShouldNotReturnWaitingTip() {
        PaiAiDemoServiceImpl pai = new PaiAiDemoServiceImpl();
        ChatItemVo item = new ChatItemVo().initQuestion("测试问题");
        ChatRecordsVo records = new ChatRecordsVo().setRecords(Collections.singletonList(item));

        AiChatStatEnum immediate = pai.doAsyncAnswer(42L, records, (state, result) -> {
        });

        assertThat(immediate).isEqualTo(AiChatStatEnum.IGNORE);
    }

    private static class FailingPostProcessService extends AbsChatService {
        @Override
        public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
            return AiChatStatEnum.END;
        }

        @Override
        public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo response,
                                            BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
            response.getRecords().get(0).initAnswer("有效回答", ChatAnswerTypeEnum.STREAM_END);
            consumer.accept(AiChatStatEnum.END, response);
            return AiChatStatEnum.IGNORE;
        }

        @Override
        public AISourceEnum source() {
            return AISourceEnum.PAI_AI;
        }

        @Override
        protected void processAfterSuccessedAnswered(Long user, ChatRecordsVo response) {
            throw new IllegalStateException("history unavailable");
        }

        @Override
        protected int queryUserdCnt(Long user) {
            return 0;
        }

        @Override
        protected int getMaxQaCnt(Long user) {
            return 5;
        }
    }
}
