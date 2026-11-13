package com.github.paicoding.forum.web.article;

import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleReadingContextDTO;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.service.article.repository.entity.*;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleMapper;
import com.github.paicoding.forum.service.article.service.ArticleEditionService;
import com.github.paicoding.forum.service.article.service.ArticleReadingAccess;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper;
import com.github.paicoding.forum.service.comment.service.CommentEditionSupport;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentEditionSupportTest {
    CommentEditionSupport support;
    CommentMapper comments;
    ArticleEditionService editions;
    ArticleDO article;
    CommentDO root;
    @BeforeEach void setup() {
        support = new CommentEditionSupport(); comments = mock(CommentMapper.class); editions = mock(ArticleEditionService.class);
        ArticleMapper articles = mock(ArticleMapper.class); ArticleReadingAccess access = mock(ArticleReadingAccess.class);
        ReflectionTestUtils.setField(support, "comments", comments); ReflectionTestUtils.setField(support, "editions", editions);
        ReflectionTestUtils.setField(support, "articles", articles); ReflectionTestUtils.setField(support, "readingAccess", access);
        article = new ArticleDO(); article.setId(7L); article.setDeleted(0); article.setStatus(1); article.setCurrentEditionId(20L);
        when(articles.selectById(7L)).thenReturn(article); when(access.canReadFull(any(), any())).thenReturn(true);
        root = new CommentDO(); root.setId(1L); root.setArticleId(7L); root.setEditionId(10L); root.setArticleDetailId(50L); root.setTopCommentId(0L); root.setDeleted(0); root.setUserId(5L);
        when(comments.selectById(1L)).thenReturn(root);
        ArticleReadingContextDTO context = new ArticleReadingContextDTO(); context.setVersioned(true); context.setSelectedEditionId(20L); context.setSelectedDetailId(100L);
        when(editions.resolve(article)).thenReturn(context);
    }
    @AfterEach void cleanup() { ArticleEditionRequest.clear(); }
    @Test void replyingToOldThreadInheritsRootNotCurrentPage() {
        CommentSaveReq req = new CommentSaveReq(); req.setArticleId(7L); req.setParentCommentId(1L); req.setEditionId(20L); req.setArticleDetailId(100L);
        support.prepare(req);
        assertEquals(10L, req.getEditionId()); assertEquals(50L, req.getArticleDetailId()); assertEquals(1L, req.getTopCommentId());
    }
    @Test void crossArticleAndForgedRootRepliesAreRejected() {
        CommentSaveReq req = new CommentSaveReq(); req.setArticleId(8L); req.setParentCommentId(1L);
        assertThrows(RuntimeException.class, () -> support.prepare(req));
        req.setArticleId(7L); req.setTopCommentId(99L);
        assertThrows(RuntimeException.class, () -> support.prepare(req));
    }
    @Test void longOpenClientMustSupplyItsActualSnapshot() {
        CommentSaveReq req = new CommentSaveReq(); req.setArticleId(7L);
        assertThrows(RuntimeException.class, () -> support.prepare(req));
        req.setEditionId(10L); req.setArticleDetailId(50L); support.prepare(req);
        verify(editions).publicSnapshot(7L, 10L, 50L);
    }
    @Test void aiUsesOriginalPublishedSnapshotAndUnknownEarlyThreadsNeverUseNewBody() {
        ArticleDetailDO detail = new ArticleDetailDO(); detail.setContent("original v1 body");
        when(editions.publicSnapshot(7L, 10L, 50L)).thenReturn(detail);
        assertEquals("original v1 body", support.aiContext(1L));
        root.setEditionId(null); root.setArticleDetailId(null);
        assertTrue(support.aiContext(1L).contains("早期讨论"));
    }
}
