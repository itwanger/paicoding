package com.github.paicoding.forum.test.seo;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.seo.Seo;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.global.SeoInjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class ArticleDescriptionSeoTest {
    @AfterEach
    public void clearContext() {
        ReqInfoContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void tutorialSummaryFeedsMetaOpenGraphAndStructuredData() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(
                new MockHttpServletRequest("GET", "/javaguide-ysppcq")));
        ReqInfoContext.addReqInfo(new ReqInfoContext.ReqInfo());
        SeoInjectService service = new SeoInjectService();
        GlobalViewConfig config = new GlobalViewConfig();
        config.setHost("https://paicoding.com");
        ReflectionTestUtils.setField(service, "globalViewConfig", config);
        ArticleDTO article = new ArticleDTO();
        article.setArticleId(42L);
        article.setUrlSlug("javaguide-ysppcq");
        article.setTitle("如何快速学习某项技术？（完结）");
        article.setSummary("从建立技术全貌、识别重复模式到运行 Hello World，介绍快速入门一项技术的方法。");
        article.setCreateTime(1794470930000L);
        article.setLastUpdateTime(1794470931000L);
        article.setCategory(new CategoryDTO(11L, "星球专栏"));
        article.setTags(Collections.emptyList());
        ColumnArticlesDTO detail = new ColumnArticlesDTO();
        detail.setArticle(article);
        ColumnDTO column = new ColumnDTO();
        column.setColumnId(12L);
        column.setColumn("Java 面试指南");
        column.setUrlSlug("javaguide");
        column.setAuthorName("沉默王二");

        service.initColumnSeo(detail, column);

        Seo seo = ReqInfoContext.getReqInfo().getSeo();
        String description = seo.getOgp().stream().filter(t -> "description".equals(t.getKey()))
                .findFirst().get().getVal();
        assertTrue(description.contains(article.getSummary()));
        assertNotEquals(article.getTitle(), description);
        assertEquals(description, seo.getOgp().stream().filter(t -> "og:description".equals(t.getKey()))
                .findFirst().get().getVal());
        assertEquals(description, seo.getJsonLd().get("description"));
    }
}
