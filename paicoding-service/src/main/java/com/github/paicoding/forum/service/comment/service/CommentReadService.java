package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;

import java.util.List;

/**
 * 评论Service接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface CommentReadService {

    /**
     * 根据评论id查询评论信息
     *
     * @param commentId
     * @return
     */
    CommentDO queryComment(Long commentId);

    /**
     * 查询文章评论列表
     *
     * @param articleId
     * @param page
     * @return
     */
    List<TopCommentDTO> getArticleComments(Long articleId, PageParam page);

    /**
     * 查询热门评论
     *
     * @param articleId
     * @return
     */
    TopCommentDTO queryHotComment(Long articleId);

    /**
     * 文章的有效评论数
     *
     * @param articleId
     * @return
     */
    int queryCommentCount(Long articleId);
}
