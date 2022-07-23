package com.github.liuyueyi.forum.service.comment;

import com.github.liuyueyi.forum.service.common.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.common.req.CommentReq;
import com.github.liuyueyi.forum.service.common.req.PageSearchReq;

import java.util.Map;

/**
 * 评论Service接口
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface CommentService {

    /**
     * 获取文章评论列表
     * @param articleId 文章ID
     * @param pageSearchReq 分页
     * @return
     */
    Map<Long, CommentTreeDTO> getCommentList(Long articleId, PageSearchReq pageSearchReq);

    /**
     * 更新/保存评论
     * @param commentReq
     * @throws Exception
     */
    void saveComment(CommentReq commentReq) throws Exception;

    /**
     * 删除评论
     * @param commentId
     * @throws Exception
     */
    void deleteComment(Long commentId) throws Exception;
}
