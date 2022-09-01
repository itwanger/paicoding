package com.github.liuyueyi.forum.service.comment;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;

import java.util.List;

/**
 * 评论Service接口
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface CommentService {

    /**
     * 查询文章评论列表
     *
     * @param articleId
     * @param page
     * @return
     */
    List<TopCommentDTO> getArticleComments(Long articleId, PageParam page);

    /**
     * 更新/保存评论
     *
     * @param commentSaveReq
     * @return
     */
    Long saveComment(CommentSaveReq commentSaveReq);

    /**
     * 删除评论
     *
     * @param commentId
     * @throws Exception
     */
    void deleteComment(Long commentId) throws Exception;

    /**
     * 文章的有效评论数
     *
     * @param articleId
     * @return
     */
    int commentCount(Long articleId);
}
