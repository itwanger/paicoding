package com.github.paicoding.forum.service.comment;

import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.comment.dto.HighlightDto;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.comment.repository.dao.CommentDao;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.impl.CommentWriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HighlightRelocationTest {
    private final CommentDao dao = mock(CommentDao.class);
    private final CommentWriteServiceImpl service = new CommentWriteServiceImpl();
    private final CommentDO row = new CommentDO();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "commentDao", dao);
        row.setId(3L);
        row.setUserId(7L);
        row.setArticleId(2L);
        row.setTopCommentId(0L);
        row.setDeleted(0);
        row.setHighlightInfo("{\"selectedText\":\"旧原文\",\"elementIndex\":8}");
        when(dao.getById(3L)).thenReturn(row);
        when(dao.replaceHighlight(eq(row), anyString())).thenAnswer(call -> {
            row.setHighlightInfo(call.getArgument(1));
            return true;
        });
    }

    private HighlightDto selection(String text) {
        HighlightDto dto = new HighlightDto();
        dto.setSelectedText(text);
        dto.setTextStart(12);
        dto.setTextFingerprint("40-ff");
        return dto;
    }

    @Test
    void preservesOriginalQuoteAndThreadOnRetry() {
        HighlightDto result = service.relocateHighlight(2L, 3L, 7L, selection("新版原文"));
        assertEquals("旧原文", result.getSelectedText());
        assertEquals("新版原文", result.getAnchoredText());
        assertEquals(result, JsonUtil.toObj(row.getHighlightInfo(), HighlightDto.class));
        assertEquals(result, service.relocateHighlight(2L, 3L, 7L, selection("新版原文")));
        assertThrows(ForumException.class, () -> service.relocateHighlight(2L, 3L, 7L, selection("过期的选择")));
        verify(dao, times(1)).replaceHighlight(eq(row), anyString());
    }

    @Test
    void rejectsOtherUsersArticlesAndDeletedThreads() {
        assertThrows(ForumException.class, () -> service.relocateHighlight(2L, 3L, 8L, selection("文本")));
        assertThrows(ForumException.class, () -> service.relocateHighlight(4L, 3L, 7L, selection("文本")));
        row.setDeleted(1);
        assertThrows(ForumException.class, () -> service.relocateHighlight(2L, 3L, 7L, selection("文本")));
        verify(dao, never()).replaceHighlight(any(), anyString());
    }
}
