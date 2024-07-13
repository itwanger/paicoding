package com.github.paicoding.forum.test.dao;

import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.test.BasicTest;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class CommentDaoTest extends BasicTest {

    @Autowired
    private CommentWriteService commentWriteService;

    @Test
//    @Transactional(rollbackFor = Exception.class)
    public void testSaveComment() throws Exception {
        CommentSaveReq commentSaveReq1 = new CommentSaveReq();
        commentSaveReq1.setArticleId(1L);
        commentSaveReq1.setCommentContent("一灰的评论1");
        commentSaveReq1.setParentCommentId(0L);
        commentSaveReq1.setUserId(2L);
        Long commentId1 = commentWriteService.saveComment(commentSaveReq1);

        CommentSaveReq commentSaveReq2 = new CommentSaveReq();
        commentSaveReq2.setArticleId(1L);
        commentSaveReq2.setCommentContent("二哥的评论1");
        commentSaveReq2.setParentCommentId(0L);
        commentSaveReq2.setUserId(3L);
        Long commentId2 = commentWriteService.saveComment(commentSaveReq2);

        CommentSaveReq commentSaveReq3 = new CommentSaveReq();
        commentSaveReq3.setArticleId(1L);
        commentSaveReq3.setCommentContent("一灰的评论2");
        commentSaveReq3.setParentCommentId(commentId1);
        commentSaveReq3.setUserId(2L);
        Long commentId3 = commentWriteService.saveComment(commentSaveReq3);

        CommentSaveReq commentSaveReq4 = new CommentSaveReq();
        commentSaveReq4.setArticleId(1L);
        commentSaveReq4.setCommentContent("二哥的评论2");
        commentSaveReq4.setParentCommentId(commentId2);
        commentSaveReq4.setUserId(3L);
        commentWriteService.saveComment(commentSaveReq4);

        CommentSaveReq commentSaveReq5 = new CommentSaveReq();
        commentSaveReq5.setArticleId(1L);
        commentSaveReq5.setCommentContent("一灰的评论3");
        commentSaveReq5.setParentCommentId(commentId3);
        commentSaveReq5.setUserId(2L);
        commentWriteService.saveComment(commentSaveReq5);
    }

    @Test
    public void testDeleteComment() throws Exception {
        commentWriteService.deleteComment(58L, 1L);
    }
}
