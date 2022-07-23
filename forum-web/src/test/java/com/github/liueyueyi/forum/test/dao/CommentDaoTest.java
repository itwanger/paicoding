package com.github.liueyueyi.forum.test.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.comment.impl.CommentServiceImpl;
import com.github.liuyueyi.forum.service.common.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.common.req.CommentReq;
import com.github.liuyueyi.forum.service.common.req.PageSearchReq;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
        PageSearchReq pageSearchReq = new PageSearchReq();
        pageSearchReq.setPageNum(1L);
        pageSearchReq.setPageSize(2L);
        Map<Long, CommentTreeDTO> commentTreeDTOList = commentService.getCommentList(12L, pageSearchReq);
        log.info("commentTreeDTOList: {}", commentTreeDTOList);
    }
}
