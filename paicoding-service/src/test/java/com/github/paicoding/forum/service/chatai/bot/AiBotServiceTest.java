package com.github.paicoding.forum.service.chatai.bot;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiBotServiceTest {

    @Mock
    private ChatFacade chatFacade;

    private AiBotService service;

    @BeforeEach
    void setUp() {
        service = new AiBotService();
        ReflectionTestUtils.setField(service, "chatFacade", chatFacade);
    }

    @Test
    void missingBotUserShouldEmitStreamEndFailure() throws Exception {
        StreamResult result = triggerAndAwait();

        assertFailure(result.item.get());
        assertThat(result.callbackCount.get()).isEqualTo(1);
        verifyNoInteractions(chatFacade);
    }

    @Test
    void emptyRecordsShouldEmitStreamEndFailure() throws Exception {
        installBotUser();
        doAnswer(invocation -> {
            Consumer<ChatRecordsVo> callback = invocation.getArgument(1);
            callback.accept(new ChatRecordsVo().setRecords(Collections.emptyList()));
            return null;
        }).when(chatFacade).autoChat(anyString(), org.mockito.ArgumentMatchers.<Consumer<ChatRecordsVo>>any());

        StreamResult result = triggerAndAwait();

        assertFailure(result.item.get());
        assertThat(result.callbackCount.get()).isEqualTo(1);
    }

    @Test
    void workerExceptionShouldEmitStreamEndFailure() throws Exception {
        installBotUser();
        when(chatFacade.autoChat(anyString(), org.mockito.ArgumentMatchers.<Consumer<ChatRecordsVo>>any()))
                .thenThrow(new IllegalStateException("provider unavailable"));

        StreamResult result = triggerAndAwait();

        assertFailure(result.item.get());
        assertThat(result.callbackCount.get()).isEqualTo(1);
    }

    @Test
    void emptyCallbackThenExceptionShouldOnlyEmitOneTerminal() throws Exception {
        installBotUser();
        CountDownLatch providerFinished = new CountDownLatch(1);
        doAnswer(invocation -> {
            try {
                Consumer<ChatRecordsVo> callback = invocation.getArgument(1);
                callback.accept(new ChatRecordsVo().setRecords(Collections.emptyList()));
                throw new IllegalStateException("failure after callback");
            } finally {
                providerFinished.countDown();
            }
        }).when(chatFacade).autoChat(anyString(), org.mockito.ArgumentMatchers.<Consumer<ChatRecordsVo>>any());

        AtomicInteger callbackCount = new AtomicInteger();
        CountDownLatch twoCallbacks = new CountDownLatch(2);
        service.triggerStream(AiBotEnum.QA_BOT, "question", "comment:1_42", item -> {
            callbackCount.incrementAndGet();
            twoCallbacks.countDown();
        });

        assertTrue(providerFinished.await(2, TimeUnit.SECONDS));
        assertFalse(twoCallbacks.await(300, TimeUnit.MILLISECONDS));
        assertThat(callbackCount.get()).isEqualTo(1);
    }

    private StreamResult triggerAndAwait() throws Exception {
        StreamResult result = new StreamResult();
        service.triggerStream(AiBotEnum.QA_BOT, "question", "comment:1_42", item -> {
            result.item.set(item);
            result.callbackCount.incrementAndGet();
            result.latch.countDown();
        });
        assertTrue(result.latch.await(2, TimeUnit.SECONDS));
        return result;
    }

    private void installBotUser() {
        Map<AiBotEnum, BaseUserInfoDTO> botUsers = new EnumMap<>(AiBotEnum.class);
        botUsers.put(AiBotEnum.QA_BOT, new BaseUserInfoDTO().setUserId(99L));
        ReflectionTestUtils.setField(service, "botUsers", botUsers);
    }

    private void assertFailure(ChatItemVo item) {
        assertThat(item).isNotNull();
        assertThat(item.getAnswerType()).isEqualTo(ChatAnswerTypeEnum.STREAM_END);
        assertThat(item.getAnswer()).isEqualTo("AI 回复生成失败，请稍后再试");
    }

    private static class StreamResult {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<ChatItemVo> item = new AtomicReference<>();
        private final AtomicInteger callbackCount = new AtomicInteger();
    }
}
