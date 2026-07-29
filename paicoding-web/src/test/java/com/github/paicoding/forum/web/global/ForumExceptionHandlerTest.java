package com.github.paicoding.forum.web.global;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 接口异常响应不能泄露内部实现细节。
 *
 * <p>线上 `/search/api/list?key=test`（缺 page 参数）曾返回
 * `非预期异常:org.springframework.web.bind.MissingServletRequestParameterException: ... at org.springframework...`，
 * 把完整 Java 堆栈、框架版本和包结构暴露给了任何匿名调用方。</p>
 *
 * @author Claude
 */
public class ForumExceptionHandlerTest {

    private final ForumExceptionHandler handler = new ForumExceptionHandler();

    @Test
    public void missingParamShouldNotLeakStackTrace() throws Exception {
        String body = resolve("/search/api/list", new MissingServletRequestParameterException("page", "Long"));

        assertNoInternals(body);
        assertTrue(body.contains("参数异常"), "应给出参数不合法的提示，实际：" + body);
    }

    @Test
    public void unexpectedErrorShouldNotLeakStackTrace() throws Exception {
        String body = resolve("/article/api/detail",
                new IllegalStateException("Duplicate entry 'x' for key 'PRIMARY' at com.mysql.jdbc"));

        assertNoInternals(body);
        assertFalse(body.contains("PRIMARY"), "不应回传底层异常原文，实际：" + body);
        assertTrue(body.contains("\"msg\":\"服务异常，请稍后重试\""), "应给出通用提示，实际：" + body);
    }

    @Test
    public void adminRequestShouldKeepRootCauseDetail() throws Exception {
        Exception wrapped = new IllegalStateException("外层包装",
                new IllegalArgumentException("Duplicate entry 'x' for key 'PRIMARY'"));

        for (String uri : new String[]{"/admin/article/save", "/api/admin/article/save"}) {
            String body = resolve(uri, wrapped);
            assertTrue(body.contains("Duplicate entry"), uri + " 后台应保留根因描述，实际：" + body);
            assertTrue(body.contains("IllegalArgumentException"), uri + " 后台应保留根因类名，实际：" + body);
            assertFalse(body.contains("\tat "), uri + " 仍不应回传堆栈，实际：" + body);
        }
    }

    @Test
    public void adminParamErrorShouldKeepDetail() throws Exception {
        String body = resolve("/api/admin/article/list", new MissingServletRequestParameterException("page", "Long"));

        assertTrue(body.contains("page"), "后台应说明缺哪个参数，实际：" + body);
        assertFalse(body.contains("\tat "), "仍不应回传堆栈，实际：" + body);
    }

    private String resolve(String uri, Exception ex) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.resolveException(request, response, null, ex);
        return response.getContentAsString();
    }

    private void assertNoInternals(String body) {
        assertFalse(body.contains("org.springframework"), "响应体泄露了框架类名：" + body);
        assertFalse(body.contains("java.lang."), "响应体泄露了异常类名：" + body);
        assertFalse(body.contains("\tat "), "响应体泄露了堆栈：" + body);
    }
}
