package com.github.liuyueyi.forum.service.user.service;


import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
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
     * 保存评论足迹
     * 1. 用户文章记录上，设置为已评论
     * 2. 若改评论为回复别人的评论，则针对父评论设置为已评论
     *
     * @param comment             保存评论入参
     * @param article             文章信息
     */
    void saveCommentFoot(CommentDO comment, ArticleDTO article);

    /**
     * 删除评论足迹
     *
     * @param comment             保存评论入参
     * @param article             文章信息
     */
    void removeCommentFoot(CommentDO comment, ArticleDTO article);


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
}
