package com.github.paicoding.forum.web.front.table;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import com.github.paicoding.forum.service.table.TableAiService;
import com.github.paicoding.forum.web.global.ForumExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TableIntegrationTest {
    MockMvc mvc;
    TableAiService ai;
    private final String body = "{\"title\":\"冲刺计划\",\"columns\":[\"任务\"],\"values\":[\"集合框架\"],\"action\":\"explain\"}";

    @BeforeEach
    void setup() {
        ai = mock(TableAiService.class);
        // Mirror QuickForumApplication#configureHandlerExceptionResolvers with ForumExceptionHandler first
        mvc = MockMvcBuilders.standaloneSetup(new TableRestController(ai))
                .setHandlerExceptionResolvers(new ForumExceptionHandler())
                .build();
        ReqInfoContext.ReqInfo info = new ReqInfoContext.ReqInfo();
        info.setUserId(10L);
        ReqInfoContext.addReqInfo(info);
    }

    @AfterEach
    void cleanup() {
        ReqInfoContext.clear();
    }

    @Test
    void anonymousRequestPreserves401UnderForumExceptionHandler() throws Exception {
        ReqInfoContext.clear();
        mvc.perform(post("/api/tables/learning-help")
                        .header("X-Pai-Table", "1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status.code").value(401))
                .andExpect(jsonPath("$.status.msg").value("请先登录"));
        verifyNoInteractions(ai);
    }

    @Test
    void rejectsCrossOriginAndAcceptsReverseProxyForwardedHost() throws Exception {
        mvc.perform(post("/api/tables/learning-help").contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tables/learning-help")
                        .header("X-Pai-Table", "1")
                        .header("Host", "paicoding.com")
                        .header("Origin", "https://evil.example")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
        verifyNoInteractions(ai);

        when(ai.learningHelp(eq(10L), eq(false), any(TableLearningReq.class))).thenReturn("ArrayList 使用动态数组。");
        mvc.perform(post("/api/tables/learning-help")
                        .header("X-Pai-Table", "1")
                        .header("Host", "127.0.0.1:8080")
                        .header("X-Forwarded-Host", "paicoding.com")
                        .header("X-Forwarded-Proto", "https")
                        .header("Origin", "https://paicoding.com")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ArrayList 使用动态数组。"))
                .andExpect(header().string("Cache-Control", "private, no-store"));
    }

    @Test
    void malformedRequestsNeverCallProvider() throws Exception {
        mvc.perform(post("/api/tables/learning-help")
                        .header("X-Pai-Table", "1")
                        .contentType("application/json")
                        .content("broken"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(ai);
    }

    @Test
    @SuppressWarnings("unchecked")
    void streamsSseEventsForLoggedInUser() throws Exception {
        when(ai.learningHelpStream(eq(10L), eq(false), any(TableLearningReq.class), any(Consumer.class), any(Runnable.class), any(Consumer.class)))
                .thenAnswer(inv -> {
                    Consumer<String> onDelta = inv.getArgument(3);
                    Runnable onComplete = inv.getArgument(4);
                    onDelta.accept("HashMap 基于数组+链表+红黑树。");
                    onComplete.run();
                    return (Runnable) () -> {};
                });

        MvcResult asyncResult = mvc.perform(post("/api/tables/learning-help/stream")
                        .header("X-Pai-Table", "1")
                        .header("Host", "paicoding.com")
                        .header("Origin", "https://paicoding.com")
                        .contentType("application/json")
                        .content(body))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult finalResult = mvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andReturn();
        String sseBody = finalResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(sseBody.contains("HashMap 基于数组+链表+红黑树。"), sseBody);
        org.junit.jupiter.api.Assertions.assertTrue(sseBody.contains("\"type\":\"done\""), sseBody);
    }

    @Test
    void returnsAgentToolResultForLoggedInUser() throws Exception {
        java.util.Map<String, Object> mockRes = new java.util.LinkedHashMap<>();
        mockRes.put("answer", "已筛选并发内容");
        mockRes.put("steps", java.util.Collections.singletonList("调整表格视图"));
        when(ai.agentHelp(eq(10L), eq(false), any(TableLearningReq.class))).thenReturn(mockRes);

        mvc.perform(post("/api/tables/agent")
                        .header("X-Pai-Table", "1")
                        .header("Host", "paicoding.com")
                        .header("Origin", "https://paicoding.com")
                        .contentType("application/json")
                        .content("{\"title\":\"冲刺计划\",\"columns\":[\"任务\"],\"values\":[\"并发\"],\"action\":\"ask\",\"question\":\"筛选并发\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.answer").value("已筛选并发内容"))
                .andExpect(jsonPath("$.result.steps[0]").value("调整表格视图"));
    }

    @Test
    void exposesCapabilitiesWithLoginFlagAndNoPersonalTableRoutes() throws Exception {
        when(ai.available()).thenReturn(true);
        mvc.perform(get("/api/tables/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.ai").value(true))
                .andExpect(jsonPath("$.result.loggedIn").value(true));

        ReqInfoContext.clear();
        mvc.perform(get("/api/tables/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.loggedIn").value(false));

        mvc.perform(post("/api/tables").header("X-Pai-Table", "1").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/tables/shared/0123456789abcdef0123456789abcdef/copy").header("X-Pai-Table", "1"))
                .andExpect(status().isNotFound());
    }
}
