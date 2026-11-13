package com.github.paicoding.forum.web.article;

import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleReadingContextDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.*;
import com.github.paicoding.forum.service.article.repository.mapper.*;
import com.github.paicoding.forum.service.article.service.ArticleEditionService;
import com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticleEditionServiceTest {
    ArticleEditionService service;
    ArticleMapper articles;
    ArticleDetailMapper details;
    ArticleEditionMapper editions;
    ArticleDO article;
    ArticleEditionDO edition;
    ArticleDetailDO published;
    ApplicationContext previousContext;

    @BeforeEach void setup() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(new org.apache.ibatis.builder.MapperBuilderAssistant(new com.baomidou.mybatisplus.core.MybatisConfiguration(), "edition-test"), ArticleEditionDO.class);
        service = new ArticleEditionService(); articles = mock(ArticleMapper.class);
        details = mock(ArticleDetailMapper.class); editions = mock(ArticleEditionMapper.class);
        ReflectionTestUtils.setField(service, "articles", articles); ReflectionTestUtils.setField(service, "details", details);
        ReflectionTestUtils.setField(service, "editions", editions); ReflectionTestUtils.setField(service, "comments", mock(CommentMapper.class));
        article = new ArticleDO(); article.setId(7L); article.setCurrentEditionId(20L); article.setStatus(1); article.setDeleted(0);
        article.setTitle("online title"); article.setUrlSlug("react-vs-toolcall"); article.setUserId(5L);
        edition = new ArticleEditionDO(); edition.setId(20L); edition.setArticleId(7L); edition.setEditionNo(2);
        edition.setEditionCode("v2"); edition.setEditionName("实战版"); edition.setPublishedDetailId(100L); edition.setDraftDetailId(101L);
        edition.setState(1); edition.setDeleted(0);
        published = snapshot(100L, "online body");
        when(articles.selectForUpdateById(7L)).thenReturn(article);
        when(editions.selectById(20L)).thenReturn(edition);
        when(editions.selectList(any())).thenReturn(Collections.singletonList(edition));
        when(details.selectById(100L)).thenReturn(published);
        ArticleDetailDO draft = snapshot(101L, "secret draft"); draft.setFirstPublishedAt(null); draft.setVersion(12L);
        when(details.selectById(101L)).thenReturn(draft); when(details.selectOne(any())).thenReturn(draft);
        when(details.insert(any())).thenAnswer(invocation -> { ((ArticleDetailDO) invocation.getArgument(0)).setId(102L); return 1; });
        previousContext = SpringUtil.getContext(); new SpringUtil().setApplicationContext(mock(ApplicationContext.class));
    }
    @AfterEach void cleanup() { ArticleEditionRequest.clear(); new SpringUtil().setApplicationContext(previousContext); }
    ArticleDetailDO snapshot(long id, String content) {
        ArticleDetailDO d = new ArticleDetailDO(); d.setId(id); d.setArticleId(7L); d.setEditionId(20L); d.setVersion(11L);
        d.setDeleted(0); d.setFirstPublishedAt(new Date()); d.setContent(content); return d;
    }
    ArticlePostReq request(String mode) {
        ArticlePostReq r = new ArticlePostReq(); r.setArticleId(7L); r.setEditionId(20L); r.setPublishMode(mode);
        r.setExpectedPublishedDetailId(100L); r.setExpectedDraftDetailId(101L); r.setTitle("changed title");
        r.setContent("changed body"); r.setEditionName("实战版"); return r;
    }
    @Test void publicReadUsesPublishedPointerAndHidesDraftMetadata() {
        ArticleReadingContextDTO context = service.resolve(article);
        assertEquals(100L, context.getSelectedDetailId()); assertNull(context.getEditions().get(0).getDraftDetailId());
        verify(details, never()).selectOne(any());
    }
    @Test void draftsAndForeignSnapshotsAreNeverPublic() {
        assertThrows(RuntimeException.class, () -> service.publicSnapshot(7L, 20L, 101L));
        published.setArticleId(8L);
        assertThrows(RuntimeException.class, () -> service.publicSnapshot(7L, 20L, 100L));
        published.setArticleId(7L); published.setEditionId(21L);
        assertThrows(RuntimeException.class, () -> service.publicSnapshot(7L, 20L, 100L));
    }
    @Test void oldPublishedSnapshotRemainsValidAfterRevision() {
        edition.setPublishedDetailId(101L);
        assertEquals("online body", service.publicSnapshot(7L, 20L, 100L).getContent());
    }
    @Test void withdrawnEditionCannotExposeItsPreviouslyPublishedSnapshot() {
        edition.setState(2);
        assertThrows(RuntimeException.class, () -> service.publicSnapshot(7L, 20L, 100L));
    }
    @Test void invalidSelectionDoesNotFallBackToLatestDraft() {
        assertThrows(RuntimeException.class, () -> service.resolve(article, 999L));
        verify(details, never()).selectOne(any());
    }
    @Test void saveDraftOnlyAppendsAndDoesNotChangePublicMetadataOrTimestamp() {
        service.save(request("SAVE_DRAFT"));
        ArgumentCaptor<ArticleDetailDO> saved = ArgumentCaptor.forClass(ArticleDetailDO.class);
        verify(details).insert(saved.capture());
        assertEquals(13L, saved.getValue().getVersion()); assertNull(saved.getValue().getFirstPublishedAt());
        assertEquals("changed body", saved.getValue().getContent()); assertNotNull(saved.getValue().getCreateTime());
        assertEquals(100L, edition.getPublishedDetailId()); assertEquals("online title", article.getTitle());
        verify(articles, never()).updateById(any()); verify(details, never()).updateById(any());
        verify(SpringUtil.getContext(), never()).publishEvent(any(org.springframework.context.ApplicationEvent.class));
    }
    @Test void staleDraftOrPublishedPointerRejectsBeforeWriting() {
        ArticlePostReq r = request("REVISE_EDITION"); r.setExpectedDraftDetailId(99L);
        assertThrows(RuntimeException.class, () -> service.save(r)); verify(details, never()).insert(any());
        r.setExpectedDraftDetailId(101L); r.setExpectedPublishedDetailId(null);
        assertThrows(RuntimeException.class, () -> service.save(r)); verify(details, never()).insert(any());
    }
    @Test void publishingHistoricalEditionDoesNotReplaceDefaultMetadata() {
        article.setCurrentEditionId(30L);
        service.save(request("REVISE_EDITION"));
        assertEquals(102L, edition.getPublishedDetailId()); assertEquals(30L, article.getCurrentEditionId());
        verify(articles, never()).updateById(any()); verify(details, never()).updateById(any());
    }
    @Test void firstPublicationOfSavedNewDraftAcceptsNullPublishedPointer() {
        article.setCurrentEditionId(30L); edition.setState(0); edition.setPublishedDetailId(null);
        ArticlePostReq req = request("REVISE_EDITION"); req.setExpectedPublishedDetailId(null);
        service.save(req);
        assertEquals(1, edition.getState()); assertEquals(102L, edition.getPublishedDetailId());
        verify(articles, never()).updateById(any());
    }
    @Test void offlineArticleCannotBeReadByPublicEditionRequests() {
        article.setStatus(0);
        assertThrows(RuntimeException.class, () -> service.resolve(article));
        verify(details, never()).selectById(any());
    }
    @Test void withdrawingDefaultRequiresPublishedReplacement() {
        assertThrows(RuntimeException.class, () -> service.withdraw(7L, 20L, null));
        assertEquals(20L, article.getCurrentEditionId()); verify(editions, never()).updateById(any());
    }
    @Test void unversionedArticleKeepsLegacyReadContract() {
        article.setCurrentEditionId(null);
        assertFalse(service.resolve(article).isVersioned()); verifyNoInteractions(editions, details);
    }
    @Test void idsAreSerializedAsStringsBeyondJavascriptSafeInteger() {
        ArticleReadingContextDTO context = new ArticleReadingContextDTO(); context.setSelectedDetailId(9007199254740993L);
        assertTrue(JsonUtil.toStr(context).contains("\"selectedDetailId\":\"9007199254740993\""));
    }
}
