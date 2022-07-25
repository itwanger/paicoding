package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentReq;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.service.comment.impl.CommentServiceImpl;
import com.github.liuyueyi.forum.service.comment.dto.CommentTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author YiHui
 * @date 2022/7/20
 */
@Slf4j
public class CommentDaoTest extends BasicTest {

    @Autowired
    private CommentServiceImpl commentService;

    public void testSaveComment() throws Exception {
        CommentReq commentReq = new CommentReq();
        commentReq.setArticleId(12L);
        commentReq.setCommentContent("comment test1");
        commentReq.setParentCommentId(0L);
        commentReq.setUserId(123L);
        commentService.saveComment(commentReq);
    }

    @Test
    public void testGetCommentList() {
        PageParam pageSearchReq = new PageParam();
        pageSearchReq.setPageNum(1L);
        pageSearchReq.setPageSize(2L);
        Map<Long, CommentTreeDTO> commentTreeDTOList = commentService.getCommentList(12L, pageSearchReq);
        log.info("commentTreeDTOList: {}", commentTreeDTOList);
    }
}
