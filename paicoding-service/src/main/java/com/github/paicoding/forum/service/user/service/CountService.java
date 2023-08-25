package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;

import java.util.List;

/**
 * 计数统计相关
 *
 * @author YiHui
 * @date 2022/9/2
 */
public interface CountService {
    /**
     * 查询文章的点赞用户头像
     *
     * @param articleId
     * @return
     */
    List<SimpleUserInfoDTO> queryPraiseUserInfosByArticleId(Long articleId);

    /**
     * 根据文章ID查询文章计数
     *
     * @param articleId
     * @return
     */
    ArticleFootCountDTO queryArticleCountInfoByArticleId(Long articleId);


    /**
     * 查询做的总阅读相关计数（当前未返回评论数）
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
     * @return
     */
    UserStatisticInfoDTO queryUserStatisticInfo(Long userId);
}
