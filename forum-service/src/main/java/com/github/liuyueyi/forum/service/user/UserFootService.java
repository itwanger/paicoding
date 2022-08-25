package com.github.liuyueyi.forum.service.user;


import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;

import java.util.List;

/**
 * 用户足迹Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserFootService {

    /**
     * 保存文章计数
     *
     * @param articleId       文章主键
     * @param userId          操作用户
     * @param operateTypeEnum 操作类型
     * @return
     */
    ArticleFootCountDTO saveArticleFoot(Long articleId, Long userId, OperateTypeEnum operateTypeEnum);

    /**
     * 根据文章ID查询文章计数
     *
     * @param articleId
     * @return
     */
    ArticleFootCountDTO queryArticleCountByArticleId(Long articleId);


    /**
     * 根据用户ID查询文章计数
     *
     * @param userId
     * @return
     */
    ArticleFootCountDTO queryArticleCountByUserId(Long userId);

    /**
     * 获取评论点赞数量
     *
     * @param commentId
     * @return
     */
    Long queryCommentPraiseCount(Long commentId);

    /**
     * 查询已读文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryReadArticleList(Long userId, PageParam pageParam);

    /**
     * 查询收藏文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryCollectionArticleList(Long userId, PageParam pageParam);

    /**
     * 保存评论足迹
     *
     * @param commentSaveReq 保存评论入参
     * @param commentId      评论ID
     * @param articleUserId  发版文章的用户ID
     */
    void saveCommentFoot(CommentSaveReq commentSaveReq, Long commentId, Long articleUserId);

    /**
     * 删除评论足迹
     *
     * @param commentDO
     * @throws Exception
     */
    void deleteCommentFoot(CommentDO commentDO) throws Exception;
}
