package com.github.paicoding.forum.web.article;

import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.vo.article.dto.*;
import com.github.paicoding.forum.web.front.article.helper.ArticleEditionRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ArticleEditionWebTest {
    @Test void requestScopeIsClearedAfterSuccessAndFailure() throws Exception {
        ArticleEditionRequestFilter filter = new ArticleEditionRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/react-vs-toolcall");
        request.addHeader("X-Article-Id", "7"); request.addHeader("X-Article-Edition", "20"); request.addHeader("X-Article-Switch", "1");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertEquals(20L, ArticleEditionRequest.forArticle(7L).getEditionId());
            assertNull(ArticleEditionRequest.forArticle(8L));
            assertTrue(ArticleEditionRequest.get().isSwitching());
        });
        assertNull(ArticleEditionRequest.get());
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            throw new IllegalStateException("render failed");
        }));
        assertNull(ArticleEditionRequest.get());
    }
    @Test void malformedHeaderIsRejectedAndAdminIsNotAffected() throws Exception {
        ArticleEditionRequestFilter filter = new ArticleEditionRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/react-vs-toolcall");
        request.addHeader("X-Article-Id", "not-an-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("malformed selection must not reach controller"));
        assertEquals(400, response.getStatus()); assertNull(ArticleEditionRequest.get());
        request.setRequestURI("/api/admin/article/detail");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> assertNull(ArticleEditionRequest.get()));
    }
    @Test void columnEditionControlsRenderWithoutAuthorAndEscapeMetadata() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        ArticleDTO article = new ArticleDTO(); article.setArticleId(7L);
        ArticleReadingContextDTO context = new ArticleReadingContextDTO(); context.setVersioned(true); context.setCurrentEditionId(20L);
        context.setSelectedEditionId(10L); context.setViewingHistoryEdition(true);
        ArticleEditionSummaryDTO old = new ArticleEditionSummaryDTO(); old.setId(10L); old.setEditionCode("v1"); old.setEditionName("<script>alert('name')</script>");
        old.setGuideTip("<script>alert('guide')</script>"); old.setLastPublishedAt(new Date());
        ArticleEditionSummaryDTO latest = new ArticleEditionSummaryDTO(); latest.setId(20L); latest.setEditionCode("v2"); latest.setEditionName("实战版");
        context.setEditions(Arrays.asList(latest, old)); article.setReadingContext(context);
        Context variables = new Context(); variables.setVariable("article", article); variables.setVariable("author", null);
        variables.setVariable("editionContextJson", com.github.paicoding.forum.core.util.JsonUtil.toStr(context));
        variables.setVariable("editionHighlightsJson", "[]");
        String data = engine.process("components/article/article-detail", Collections.singleton("#articleEditionData"), variables);
        assertTrue(data.contains("data-context="));
        assertTrue(data.contains("&quot;selectedEditionId&quot;"));
        assertFalse(data.contains("<script>alert"));
        String html = engine.process("components/article/article-detail", Collections.singleton("#articleEditionControls"), variables);
        assertTrue(html.contains("当前版")); assertTrue(html.contains("aria-checked=\"true\"")); assertTrue(html.contains("data-edition-choice=\"10\""));
        assertFalse(html.contains("<script>alert")); assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("?v="));
    }

    @Test void earlyDiscussionKeepsItsLabelAndCountUsesTheSelectedScope() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO comment =
                new com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO();
        comment.setCommentId(42L);
        Context variables = new Context(); variables.setVariable("comment", comment);
        String label = engine.process("components/comment/comment-item", Collections.singleton(".comment-edition-label"), variables);
        // An empty attribute value is removed by Thymeleaf; the client needs a non-empty sentinel.
        assertTrue(label.contains("data-comment-edition=\"legacy\""));
        ArticleDTO article = new ArticleDTO();
        ArticleReadingContextDTO reading = new ArticleReadingContextDTO(); reading.setVersioned(true);
        article.setReadingContext(reading); variables.setVariable("article", article);
        variables.setVariable("topCommentTotal", 1);
        variables.setVariable("comments", Collections.singletonList(comment));
        reading.setCurrentCommentCount(1); reading.setHistoryCommentCount(2);
        reading.setDiscussionScope("history");
        String tabs = engine.process("components/comment/comment-list", Collections.singleton("#editionDiscussionScope"), variables);
        assertTrue(tabs.contains("data-scope=\"history\""));
        assertTrue(tabs.contains("data-current-count=\"1\""));
        assertTrue(tabs.contains("data-history-count=\"2\""));
        assertTrue(tabs.contains("aria-pressed=\"true\""));
    }
    @Test void discussionFiltersFollowTheComposerEvenWhenTheListIsEmpty() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        Context variables = new Context();
        variables.setVariable("global", Collections.singletonMap("isLogin", false));
        ArticleDTO article = new ArticleDTO(); article.setArticleId(7L);
        ArticleReadingContextDTO reading = new ArticleReadingContextDTO();
        reading.setVersioned(true); reading.setCurrentCommentCount(0); reading.setHistoryCommentCount(2);
        article.setReadingContext(reading); variables.setVariable("article", article);
        variables.setVariable("comments", Collections.emptyList()); variables.setVariable("topCommentTotal", 0);
        String html = engine.process("components/comment/comment-list", new HashSet<>(Arrays.asList(
                ".comment-write-wrap", "#editionDiscussionScope", "#editionCommentResults")), variables);
        assertTrue(html.indexOf("id=\"commentContent\"") < html.indexOf("id=\"editionDiscussionScope\""));
        assertTrue(html.indexOf("id=\"editionDiscussionScope\"") < html.indexOf("id=\"editionCommentResults\""));
        assertTrue(html.contains("暂无评论"));
        assertTrue(html.contains("class=\"all-comment-items\""));
    }

}
