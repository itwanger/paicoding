package com.github.paicoding.forum.test.seo;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.seo.SeoTagVo;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.front.user.vo.UserHomeVo;
import com.github.paicoding.forum.web.global.SeoInjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeoInjectServiceTest {

    @AfterEach
    public void clearContext() {
        RequestContextHolder.resetRequestAttributes();
        ReqInfoContext.clear();
    }

    @Test
    public void userHomeWithoutQueryStaysIndexable() {
        UserHomeVo vo = bindRequest("/user/123", null);

        newService().initUserSeo(vo);

        assertEquals("all", robotsValue(vo));
    }

    @Test
    public void userHomeWithStateQueryGetsNoindexFollow() {
        UserHomeVo vo = bindRequest("/user/123", "homeSelectType=follow");

        newService().initUserSeo(vo);

        assertEquals("noindex, follow", robotsValue(vo));
    }

    @Test
    public void userHomeFollowSelectTypeQueryGetsNoindexFollow() {
        UserHomeVo vo = bindRequest("/user/123", "followSelectType=fans");

        newService().initUserSeo(vo);

        assertEquals("noindex, follow", robotsValue(vo));
    }

    private UserHomeVo bindRequest(String uri, String query) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setServerName("paicoding.com");
        if (query != null) {
            request.setQueryString(query);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ReqInfoContext.addReqInfo(new ReqInfoContext.ReqInfo());

        UserStatisticInfoDTO userInfo = new UserStatisticInfoDTO();
        userInfo.setUserId(123L);
        userInfo.setUserName("测试用户");
        userInfo.setProfile("简介");

        UserHomeVo vo = new UserHomeVo();
        vo.setUserHome(userInfo);
        return vo;
    }

    private SeoInjectService newService() {
        SeoInjectService service = new SeoInjectService();
        GlobalViewConfig config = new GlobalViewConfig();
        config.setHost("https://paicoding.com");
        ReflectionTestUtils.setField(service, "globalViewConfig", config);
        return service;
    }

    private String robotsValue(UserHomeVo vo) {
        List<SeoTagVo> ogp = ReqInfoContext.getReqInfo().getSeo().getOgp();
        Optional<SeoTagVo> robots = ogp.stream().filter(t -> "robots".equals(t.getKey())).findFirst();
        return robots.map(SeoTagVo::getVal).orElse(null);
    }
}
