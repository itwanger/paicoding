package com.github.paicoding.forum.test.miniprogram;

import com.github.paicoding.forum.web.hook.filter.ReqRecordFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReqRecordFilterMiniProgramTest {

    @Test
    public void shouldReadMiniProgramDeviceHintFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addHeader("X-Pai-Device-Id", "fp-mini-device-123456");

        String hint = resolveClientDeviceHint(request);

        assertEquals("fp-mini-device-123456", hint);
    }

    @Test
    public void shouldPreferHeaderDeviceHintOverParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addParameter("deviceId", "fp-param-device-123456");
        request.addHeader("X-Pai-Device-Id", "fp-header-device-123456");

        String hint = resolveClientDeviceHint(request);

        assertEquals("fp-header-device-123456", hint);
    }

    @Test
    public void shouldFallbackToLegacyDeviceParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addParameter("deviceId", "fp-param-device-123456");

        String hint = resolveClientDeviceHint(request);

        assertEquals("fp-param-device-123456", hint);
    }

    @Test
    public void shouldRejectUnsafeDeviceHint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addHeader("X-Pai-Device-Id", "fp-mini-device<script>");

        String hint = resolveClientDeviceHint(request);

        assertEquals("", hint);
    }

    @Test
    public void shouldRejectOversizedDeviceHint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mini/api/user/me");
        request.addHeader("X-Pai-Device-Id", "fp-abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        String hint = resolveClientDeviceHint(request);

        assertEquals("", hint);
    }

    private String resolveClientDeviceHint(MockHttpServletRequest request) {
        return ReflectionTestUtils.invokeMethod(new ReqRecordFilter(), "resolveClientDeviceHint", request);
    }
}
