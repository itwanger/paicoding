package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.vo.SubCommentListVO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;

import java.util.List;

/**
 * 评论Service接口
 *
 * @author louzai
 * @date 2022-07-24
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
     * 查询文章的划线评论
     *
     * @param articleId
     * @return
     */
    List<TopCommentDTO> queryHighlightComments(Long articleId);


    /**
     * 查询顶级评论及之下的所有评论
     *
     * @param commentId
     * @return
     */
    TopCommentDTO queryTopComments(Long commentId);

    /**
     * 文章的有效评论数
     *
     * @param articleId
     * @return
     */
    int queryCommentCount(Long articleId);

    /**
     * 一级评论总数
     *
     * @param articleId 文章ID
     * @return 一级评论总数
     */
    int queryTopCommentCount(Long articleId);

    /**
     * 分页查询子评论
     *
     * @param topCommentId 一级评论ID
     * @param page 分页参数
     * @return 子评论列表（含分页信息）
     */
    SubCommentListVO getSubComments(Long topCommentId, PageParam page);

    /**
     * 统计一级评论的子评论数量
     *
     * @param topCommentId 一级评论ID
     * @return 子评论数量
     */
    int countSubComments(Long topCommentId);
}
