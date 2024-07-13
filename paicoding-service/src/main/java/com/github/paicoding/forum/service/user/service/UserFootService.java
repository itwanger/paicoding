package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserFootStatisticDTO;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;

import java.util.List;

/**
 * 用户足迹Service接口
 *
 * @author XuYifei
 * @date 2024-07-12
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
     * 保存评论足迹
     * 1. 用户文章记录上，设置为已评论
     * 2. 若改评论为回复别人的评论，则针对父评论设置为已评论
     *
     * @param comment             保存评论入参
     * @param articleAuthor       文章作者
     * @param parentCommentAuthor 父评论作者
     */
    void saveCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor);

    /**
     * 删除评论足迹
     *
     * @param comment             保存评论入参
     * @param articleAuthor       文章作者
     * @param parentCommentAuthor 父评论作者
     */
    void removeCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor);


    /**
     * 查询已读文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<Long> queryUserReadArticleList(Long userId, PageParam pageParam);

    /**
     * 查询收藏文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<Long> queryUserCollectionArticleList(Long userId, PageParam pageParam);

    /**
     * 查询文章的点赞用户信息
     *
     * @param articleId
     * @return
     */
    List<SimpleUserInfoDTO> queryArticlePraisedUsers(Long articleId);


    /**
     * 查询用户记录，用于判断是否点过赞、是否评论、是否收藏过
     *
     * @param documentId
     * @param type
     * @param userId
     * @return
     */
    UserFootDO queryUserFoot(Long documentId, Integer type, Long userId);

    UserFootStatisticDTO getFootCount();
}
