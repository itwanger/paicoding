package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
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
        CommentSaveReq commentSaveReq = new CommentSaveReq();
        commentSaveReq.setArticleId(12L);
        commentSaveReq.setCommentContent("comment test1");
        commentSaveReq.setParentCommentId(0L);
        commentSaveReq.setUserId(123L);
        commentService.saveComment(commentSaveReq);
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
