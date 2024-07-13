package com.github.paicoding.forum.service.statistics.service;

import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;

/**
 * 计数统计相关
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface CountService {
    /**
     * 根据文章ID查询文章计数
     * 本方法直接基于db进行查询相关信息，改用下面的 queryArticleStatisticInfo() 方法进行替换
     *
     * @param articleId
     * @return
     */
    @Deprecated
    ArticleFootCountDTO queryArticleCountInfoByArticleId(Long articleId);


    /**
     * 查询用户总阅读相关计数（当前未返回评论数）
     * 本方法直接基于db进行查询相关信息，改用下面的 queryUserStatisticInfo() 方法进行替换
     *
     * @param userId
     * @return
     */
    @Deprecated
    ArticleFootCountDTO queryArticleCountInfoByUserId(Long userId);

    /**
     * 获取评论点赞数量
     *
     * @param commentId
     * @return
     */
    Long queryCommentPraiseCount(Long commentId);


    /**
     * 查询用户的相关统计信息
     *
     * @param userId
     * @return 返回用户的 收藏、点赞、文章、粉丝、关注，总的文章阅读数
     */
    UserStatisticInfoDTO queryUserStatisticInfo(Long userId);

    /**
     * 查询文章相关的统计信息
     *
     * @param articleId
     * @return 返回文章的 收藏、点赞、评论、阅读数
     */
    ArticleFootCountDTO queryArticleStatisticInfo(Long articleId);


    /**
     * 文章计数+1
     *
     * @param authorUserId 作者
     * @param articleId    文章
     * @return 计数器
     */
    void incrArticleReadCount(Long authorUserId, Long articleId);

    /**
     * 刷新用户的统计信息
     *
     * @param userId
     */
    void refreshUserStatisticInfo(Long userId);

    /**
     * 刷新文章的统计信息
     *
     * @param articleId
     */
    void refreshArticleStatisticInfo(Long articleId);
}
