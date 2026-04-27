package com.github.paicoding.forum.test.seo;

import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.hook.filter.CanonicalUrlRedirectFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CanonicalUrlRedirectFilterTest {

    @Test
    public void redirectHomeHtmlToCanonicalHome() throws Exception {
        MockHttpServletResponse response = doFilter(request("/home.html"));

        assertEquals(301, response.getStatus());
        assertEquals("https://paicoding.com/", response.getHeader("Location"));
    }

    @Test
    public void redirectArticleIdWithTextSuffixToNumericArticle() throws Exception {
        MockHttpServletRequest request = request("/article/detail/2607900057933824Codex");
        request.setQueryString("from=bad");

        MockHttpServletResponse response = doFilter(request);

        assertEquals(301, response.getStatus());
        assertEquals("https://paicoding.com/article/detail/2607900057933824", response.getHeader("Location"));
    }

    @Test
    public void redirectNestedRelativeArticleDetailToCanonicalArticle() throws Exception {
        MockHttpServletResponse response = doFilter(request("/column/15/article/detail/142"));

        assertEquals(301, response.getStatus());
        assertEquals("https://paicoding.com/article/detail/142", response.getHeader("Location"));
    }

    @Test
    public void userStateQueryAddsNoindexWithoutRedirecting() throws Exception {
        MockHttpServletRequest request = request("/user/123");
        request.setQueryString("homeSelectType=follow&followSelectType=fans");

        MockHttpServletResponse response = doFilter(request);

        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("Location"));
        assertEquals("noindex, follow", response.getHeader("X-Robots-Tag"));
    }

    @Test
    public void redirectWwwHostToCanonicalHost() throws Exception {
        MockHttpServletRequest request = request("/article/detail/142");
        request.setServerName("www.paicoding.com");

        MockHttpServletResponse response = doFilter(request);

        assertEquals(301, response.getStatus());
        assertEquals("https://paicoding.com/article/detail/142", response.getHeader("Location"));
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setServerName("paicoding.com");
        return request;
    }

    private MockHttpServletResponse doFilter(MockHttpServletRequest request) throws Exception {
        CanonicalUrlRedirectFilter filter = new CanonicalUrlRedirectFilter();
        GlobalViewConfig config = new GlobalViewConfig();
        config.setHost("https://paicoding.com");
        ReflectionTestUtils.setField(filter, "globalViewConfig", config);

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
