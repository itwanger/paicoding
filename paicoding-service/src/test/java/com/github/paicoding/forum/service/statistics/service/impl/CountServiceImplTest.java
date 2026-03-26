package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.service.article.repository.mapper.ReadCountMapper;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.statistics.constants.CountConstants;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserFootDao;
import com.github.paicoding.forum.service.user.repository.dao.UserRelationDao;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountServiceImplTest {

    @Mock
    private UserFootDao userFootDao;
    @Mock
    private UserRelationDao userRelationDao;
    @Mock
    private ArticleDao articleDao;
    @Mock
    private CommentReadService commentReadService;
    @Mock
    private UserDao userDao;
    @Mock
    private ReadCountMapper readCountMapper;
    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;
    @Mock
    private Cursor<String> cursor;

    private CountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new CountServiceImpl(userFootDao));
        ReflectionTestUtils.setField(service, "userRelationDao", userRelationDao);
        ReflectionTestUtils.setField(service, "articleDao", articleDao);
        ReflectionTestUtils.setField(service, "commentReadService", commentReadService);
        ReflectionTestUtils.setField(service, "userDao", userDao);
        ReflectionTestUtils.setField(service, "readCountMapper", readCountMapper);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
    }

    @Test
    void scanKeysShouldStripPaiPrefix() throws Exception {
        when(stringRedisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("pai_article_statistic_101", "pai_article_statistic_102");

        Set<String> keys = service.scanKeys(CountConstants.ARTICLE_STATISTIC_INFO + "*");

        assertThat(keys).containsExactlyInAnyOrder("article_statistic_101", "article_statistic_102");
        verify(cursor).close();
    }

    @Test
    void syncArticleReadCountToDbShouldOnlyPersistPositiveCounts() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.add("article_statistic_101");
        keys.add("article_statistic_bad");
        keys.add("article_statistic_102");
        keys.add("article_statistic_103");

        doReturn(keys).when(service).scanKeys(CountConstants.ARTICLE_STATISTIC_INFO + "*");
        doReturn(8).when(service).getArticleReadCount("article_statistic_101");
        doReturn(0).when(service).getArticleReadCount("article_statistic_102");
        doReturn(null).when(service).getArticleReadCount("article_statistic_103");

        service.syncArticleReadCountToDb();

        verify(readCountMapper).insertOrUpdate(101L, DocumentTypeEnum.ARTICLE.getCode(), 8);
        verify(service, never()).getArticleReadCount("article_statistic_bad");
        verifyNoMoreInteractions(readCountMapper);
    }

    @Test
    void readCountMapperShouldUpsertAbsoluteCount() throws NoSuchMethodException {
        Method method = ReadCountMapper.class.getMethod("insertOrUpdate", Long.class, Integer.class, Integer.class);
        org.apache.ibatis.annotations.Insert insert = method.getAnnotation(org.apache.ibatis.annotations.Insert.class);

        assertThat(insert).isNotNull();
        assertThat(insert.value()).hasSize(1);
        assertThat(insert.value()[0])
                .contains("ON DUPLICATE KEY UPDATE cnt = #{cnt}")
                .doesNotContain("cnt = cnt + #{cnt}");
    }
}
