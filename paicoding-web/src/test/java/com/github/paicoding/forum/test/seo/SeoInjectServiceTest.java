package com.github.paicoding.forum.test.seo;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.seo.Seo;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void columnHomeSeoUsesProjectCollectionKeywords() {
        bindReqInfo("/column", null);
        ColumnDTO column = new ColumnDTO();
        column.setColumn("派聪明 RAG");
        column.setIntroduction("AI 知识库项目教程");
        column.setUrlSlug("what-is-paismart");

        newService().initColumnHomeSeo(Collections.singletonList(column));

        Seo seo = ReqInfoContext.getReqInfo().getSeo();
        assertEquals("AI 实战项目教程：派聪明 RAG、PaiFlow、PaiCLI、技术派", tagValue(seo, "title"));
        assertTrue(tagValue(seo, "description").contains("派聪明 RAG"));
        assertTrue(tagValue(seo, "keywords").contains("派聪明"));
        assertEquals("CollectionPage", seo.getJsonLd().get("@type"));
    }

    @Test
    public void columnLandingSeoIncludesColumnAndArticleList() {
        bindReqInfo("/column/what-is-paismart", null);
        ColumnDTO column = new ColumnDTO();
        column.setColumn("派聪明 RAG");
        column.setIntroduction("从零实现企业级 RAG 知识库项目。");
        column.setCover("https://paicoding.com/img/paismart.png");

        SimpleArticleDTO article = new SimpleArticleDTO();
        article.setTitle("派聪明 RAG 项目介绍");
        article.setUrlSlug("what-is-paismart");

        newService().initColumnLandingSeo(column, Arrays.asList(article));

        Seo seo = ReqInfoContext.getReqInfo().getSeo();
        assertEquals("派聪明 RAG：项目介绍、源码教程、学习路线和面试指南", tagValue(seo, "title"));
        assertTrue(tagValue(seo, "description").contains("企业级 RAG"));
        assertTrue(tagValue(seo, "keywords").contains("派聪明"));
        assertEquals("Course", seo.getJsonLd().get("@type"));
        assertTrue(seo.getJsonLd().containsKey("hasPart"));
    }

    private UserHomeVo bindRequest(String uri, String query) {
        bindReqInfo(uri, query);

        UserStatisticInfoDTO userInfo = new UserStatisticInfoDTO();
        userInfo.setUserId(123L);
        userInfo.setUserName("测试用户");
        userInfo.setProfile("简介");

        UserHomeVo vo = new UserHomeVo();
        vo.setUserHome(userInfo);
        return vo;
    }

    private void bindReqInfo(String uri, String query) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setServerName("paicoding.com");
        if (query != null) {
            request.setQueryString(query);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ReqInfoContext.addReqInfo(new ReqInfoContext.ReqInfo());
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

    private String tagValue(Seo seo, String key) {
        Optional<SeoTagVo> tag = seo.getOgp().stream().filter(t -> key.equals(t.getKey())).findFirst();
        return tag.map(SeoTagVo::getVal).orElse(null);
    }
}
