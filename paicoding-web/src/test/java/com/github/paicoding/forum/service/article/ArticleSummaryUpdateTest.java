package com.github.paicoding.forum.service.article;

import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.impl.ArticleSettingServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/** Metadata updates must not enter the body/image rewriting path. */
public class ArticleSummaryUpdateTest {
    @Test
    public void summaryOnlyUpdatePreservesArticleMetadataAndNeverWritesContent() {
        ArticleDO article = article();
        ArticleDao dao = mock(ArticleDao.class);
        when(dao.getById(42L)).thenReturn(article);
        ArticleSettingServiceImpl service = new ArticleSettingServiceImpl();
        ReflectionTestUtils.setField(service, "articleDao", dao);
        ArticlePostReq req = request();
        req.setSummary("通过网关过滤器记录接口耗时，并配置请求黑白名单。");

        service.updateArticle(req);

        assertEquals(req.getSummary(), article.getSummary());
        assertEquals("原标题", article.getTitle());
        assertEquals("短标题", article.getShortTitle());
        assertEquals("original-slug", article.getUrlSlug());
        assertEquals(Integer.valueOf(3), article.getReadType());
        assertEquals(Integer.valueOf(1), article.getStatus());
        verify(dao).getById(42L);
        verify(dao).updateById(article);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void legacyStatusUpdateWithoutSummaryPreservesExistingSummary() {
        ArticleDO article = article();
        ArticleDao dao = mock(ArticleDao.class);
        when(dao.getById(42L)).thenReturn(article);
        ArticleSettingServiceImpl service = new ArticleSettingServiceImpl();
        ReflectionTestUtils.setField(service, "articleDao", dao);
        ArticlePostReq req = request();
        req.setStatus(0);

        service.updateArticle(req);

        assertEquals("原摘要", article.getSummary());
        assertEquals(Integer.valueOf(0), article.getStatus());
        verify(dao).getById(42L);
        verify(dao).updateById(article);
        verifyNoMoreInteractions(dao);
    }

    private ArticlePostReq request() {
        ArticlePostReq req = new ArticlePostReq();
        req.setArticleId(42L);
        req.setStatus(1);
        return req;
    }

    private ArticleDO article() {
        ArticleDO article = new ArticleDO();
        article.setId(42L);
        article.setTitle("原标题");
        article.setShortTitle("短标题");
        article.setUrlSlug("original-slug");
        article.setSummary("原摘要");
        article.setReadType(3);
        article.setStatus(1);
        return article;
    }
}
