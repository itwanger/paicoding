package com.github.paicoding.forum.web.article;

import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.article.service.impl.ArticleReadServiceImpl;
import com.github.paicoding.forum.service.sensitive.service.SensitiveBypassService;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class ArticleEditionReadSideEffectsTest {
    @AfterEach void cleanup() { ArticleEditionRequest.clear(); }
    @Test void editionSwitchReadsFootprintsWithoutCountingAnotherVisit() {
        ArticleReadServiceImpl service = spy(new ArticleReadServiceImpl());
        ArticleDTO article = new ArticleDTO(); article.setArticleId(7L); article.setAuthor(5L);
        doReturn(article).when(service).queryDetailArticleInfo(7L);
        CountService counts = mock(CountService.class); UserFootService feet = mock(UserFootService.class);
        SensitiveBypassService bypass = mock(SensitiveBypassService.class);
        when(bypass.shouldBypassByUserId(5L)).thenReturn(true);
        ReflectionTestUtils.setField(service, "countService", counts);
        ReflectionTestUtils.setField(service, "userFootService", feet);
        ReflectionTestUtils.setField(service, "sensitiveBypassService", bypass);
        ArticleEditionRequest selection = new ArticleEditionRequest(); selection.setArticleId(7L); selection.setSwitching(true);
        ArticleEditionRequest.set(selection);
        service.queryFullArticleInfo(7L, 42L);
        verify(counts, never()).incrArticleReadCount(anyLong(), anyLong());
        verify(feet).queryUserFoot(eq(7L), anyInt(), eq(42L));
        verify(feet, never()).saveOrUpdateUserFoot(any(), anyLong(), anyLong(), anyLong(), any());
        ArticleEditionRequest.clear();
        service.queryFullArticleInfo(7L, null);
        verify(counts).incrArticleReadCount(5L, 7L);
    }
}
