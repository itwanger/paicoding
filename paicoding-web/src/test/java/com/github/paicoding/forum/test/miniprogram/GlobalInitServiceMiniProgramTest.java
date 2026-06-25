package com.github.paicoding.forum.test.miniprogram;

import com.github.paicoding.forum.web.global.GlobalInitService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GlobalInitServiceMiniProgramTest {

    @Test
    public void shouldReadBearerTokenOnlyForMiniApi() {
        GlobalInitService service = new GlobalInitService();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addHeader("Authorization", "bearer mini-token");

        String token = ReflectionTestUtils.invokeMethod(service, "findSessionFromHeader", request);

        assertEquals("mini-token", token);
    }

    @Test
    public void shouldIgnoreHeaderTokenOutsideMiniApi() {
        GlobalInitService service = new GlobalInitService();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/index");
        request.addHeader("Authorization", "Bearer admin-token");

        String token = ReflectionTestUtils.invokeMethod(service, "findSessionFromHeader", request);

        assertNull(token);
    }
}
