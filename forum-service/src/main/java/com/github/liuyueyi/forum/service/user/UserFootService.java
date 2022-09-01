package com.github.liuyueyi.forum.service.user;


import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;

import java.util.List;

/**
 * 用户足迹Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserFootService {
    /**
     * 保存或更新状态信息
     *
     * @param documentType    文档类型：博文 + 评论
     * @param documentId      文档id
     * @param authorId        作者
     * @param userId          操作人
     * @param operateTypeEnum 操作类型：点赞，评论，收藏等
     * @return
     */
    UserFootDO saveOrUpdateUserFoot(DocumentTypeEnum documentType, Long documentId, Long authorId, Long userId, OperateTypeEnum operateTypeEnum);

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
     * @param comment 保存评论入参
     */
    void saveCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor);

    /**
     * 删除评论足迹
     *
     * @param commentDO
     * @throws Exception
     */
    void deleteCommentFoot(CommentDO commentDO, Long articleAuthor, Long parentCommentAuthor);
}
