package com.github.paicoding.forum.web.front.comment.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentRestControllerAiStreamTest {
    private MockMvc mockMvc;
    private AiBots aiBots;
    private CommentWriteService commentWriteService;

    @BeforeEach
    void setUp() {
        ArticleReadService articleReadService = Mockito.mock(ArticleReadService.class);
        Mockito.when(articleReadService.queryBasicArticle(1L)).thenReturn(new ArticleDO());

        commentWriteService = Mockito.mock(CommentWriteService.class);
        Mockito.when(commentWriteService.saveComment(any())).thenReturn(7L);

        aiBots = Mockito.mock(AiBots.class);
        Mockito.when(aiBots.getBotUser(AiBotEnum.QA_BOT)).thenReturn(new BaseUserInfoDTO().setUserId(99L));

        CommentRestController controller = new CommentRestController();
        ReflectionTestUtils.setField(controller, "articleReadService", articleReadService);
        ReflectionTestUtils.setField(controller, "commentWriteService", commentWriteService);
        ReflectionTestUtils.setField(controller, "aiBots", aiBots);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(42L);
        ReqInfoContext.addReqInfo(reqInfo);
    }

    @AfterEach
    void tearDown() {
        ReqInfoContext.clear();
    }

    @Test
    void synchronousAiStartFailureShouldEndWithErrorEvent() throws Exception {
        Mockito.doThrow(new IllegalStateException("provider unavailable"))
                .when(aiBots)
                .triggerStream(
                        eq(AiBotEnum.QA_BOT),
                        anyString(),
                        anyString(),
                        Mockito.<Consumer<ChatItemVo>>any(),
                        Mockito.<Supplier<String>>any());

        String body = performHighlightAiRequest();

        assertTrue(body.contains("\"type\":\"comment\""));
        assertTrue(body.contains("\"type\":\"start\""));
        assertTrue(body.contains("\"type\":\"error\""));
        assertEquals(1, countMatches(body, "\"type\":\"error\""));
    }

    @Test
    void nullAiCallbackShouldEndWithErrorEvent() throws Exception {
        Mockito.doAnswer(invocation -> {
                    Consumer<ChatItemVo> consumer = invocation.getArgument(3);
                    consumer.accept(null);
                    return null;
                })
                .when(aiBots)
                .triggerStream(
                        eq(AiBotEnum.QA_BOT),
                        anyString(),
                        anyString(),
                        Mockito.<Consumer<ChatItemVo>>any(),
                        Mockito.<Supplier<String>>any());

        String body = performHighlightAiRequest();

        assertTrue(body.contains("\"type\":\"error\""));
        assertEquals(1, countMatches(body, "\"type\":\"error\""));
    }

    @Test
    void successfulTerminalShouldSaveReplyAndEndWithDoneEvent() throws Exception {
        Mockito.doAnswer(invocation -> {
                    Consumer<ChatItemVo> consumer = invocation.getArgument(3);
                    consumer.accept(new ChatItemVo().initAnswer("有效回答", ChatAnswerTypeEnum.STREAM_END));
                    return null;
                })
                .when(aiBots)
                .triggerStream(
                        eq(AiBotEnum.QA_BOT),
                        anyString(),
                        anyString(),
                        Mockito.<Consumer<ChatItemVo>>any(),
                        Mockito.<Supplier<String>>any());

        String body = performHighlightAiRequest();

        assertEquals(1, countMatches(body, "\"type\":\"done\""));
        assertEquals(0, countMatches(body, "\"type\":\"error\""));
        verify(commentWriteService, times(2)).saveComment(any());
    }

    @Test
    void terminalCallbackThenStartExceptionShouldStillEmitOneTerminal() throws Exception {
        Mockito.doAnswer(invocation -> {
                    Consumer<ChatItemVo> consumer = invocation.getArgument(3);
                    consumer.accept(new ChatItemVo().initAnswer("有效回答", ChatAnswerTypeEnum.STREAM_END));
                    throw new IllegalStateException("failure after terminal callback");
                })
                .when(aiBots)
                .triggerStream(
                        eq(AiBotEnum.QA_BOT),
                        anyString(),
                        anyString(),
                        Mockito.<Consumer<ChatItemVo>>any(),
                        Mockito.<Supplier<String>>any());

        String body = performHighlightAiRequest();

        assertEquals(1, countMatches(body, "\"type\":\"done\""));
        assertEquals(0, countMatches(body, "\"type\":\"error\""));
        verify(commentWriteService, times(2)).saveComment(any());
    }

    @Test
    void replySaveFailureShouldEndWithOneErrorEvent() throws Exception {
        Mockito.when(commentWriteService.saveComment(any()))
                .thenReturn(7L)
                .thenThrow(new IllegalStateException("database unavailable"));
        Mockito.doAnswer(invocation -> {
                    Consumer<ChatItemVo> consumer = invocation.getArgument(3);
                    consumer.accept(new ChatItemVo().initAnswer("有效回答", ChatAnswerTypeEnum.STREAM_END));
                    return null;
                })
                .when(aiBots)
                .triggerStream(
                        eq(AiBotEnum.QA_BOT),
                        anyString(),
                        anyString(),
                        Mockito.<Consumer<ChatItemVo>>any(),
                        Mockito.<Supplier<String>>any());

        String body = performHighlightAiRequest();

        assertEquals(0, countMatches(body, "\"type\":\"done\""));
        assertEquals(1, countMatches(body, "\"type\":\"error\""));
        verify(commentWriteService, times(2)).saveComment(any());
    }

    private String performHighlightAiRequest() throws Exception {
        String json = "{"
                + "\"articleId\":1,"
                + "\"highlight\":{\"selectedText\":\"测试划线内容\"},"
                + "\"bot\":\"QA_BOT\","
                + "\"commentContent\":\"@派聪明 测试问题\","
                + "\"question\":\"测试问题\","
                + "\"requestId\":\"req-1\""
                + "}";

        MvcResult asyncResult = mockMvc.perform(post("/comment/api/highlightAiStream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult result = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private int countMatches(String value, String target) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(target, offset)) >= 0) {
            count++;
            offset += target.length();
        }
        return count;
    }
}
