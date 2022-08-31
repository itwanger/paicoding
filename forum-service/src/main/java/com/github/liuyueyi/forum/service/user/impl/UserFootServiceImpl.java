package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.*;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.article.repository.ArticleRepository;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.user.UserFootService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户足迹Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserFootServiceImpl implements UserFootService {

    @Resource
    private UserFootMapper userFootMapper;

    @Override
    public ArticleFootCountDTO saveArticleFoot(Long articleId, Long author, Long userId, OperateTypeEnum operateTypeEnum) {
        if (userId != null) {
            // 未登录时，不更新对应的足迹内容
            doSaveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, author, articleId, userId, operateTypeEnum);
        }
        return userFootMapper.queryCountByArticle(articleId);
    }

    /**
     * 保存或更新状态信息
     *
     * @param documentType    文档类型：博文 + 评论
     * @param documentId      文档id
     * @param authorId        作者
     * @param userId          操作人
     * @param operateTypeEnum 操作类型：点赞，评论，收藏等
     */
    private void doSaveOrUpdateUserFoot(DocumentTypeEnum documentType, Long documentId,
                                        Long authorId, Long userId, OperateTypeEnum operateTypeEnum) {
        // 查询是否有该足迹
        UserFootDO readUserFootDO = userFootMapper.queryFootByDocumentInfo(documentId, documentType.getCode(), userId);
        if (readUserFootDO == null) {
            readUserFootDO = new UserFootDO();
            readUserFootDO.setUserId(userId);
            readUserFootDO.setDocumentId(documentId);
            readUserFootDO.setDocumentType(documentType.getCode());
            readUserFootDO.setDocumentUserId(authorId);
            setUserFootStat(readUserFootDO, operateTypeEnum);
            userFootMapper.insert(readUserFootDO);
        } else {
            setUserFootStat(readUserFootDO, operateTypeEnum);
            userFootMapper.updateById(readUserFootDO);
        }
    }

    private UserFootDO setUserFootStat(UserFootDO userFootDO, OperateTypeEnum operateTypeEnum) {
        if (operateTypeEnum == OperateTypeEnum.READ) {
            userFootDO.setReadStat(ReadStatEnum.READ.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.PRAISE) {
            userFootDO.setPraiseStat(PraiseStatEnum.PRAISE.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.CANCEL_PRAISE) {
            userFootDO.setPraiseStat(PraiseStatEnum.CANCEL_PRAISE.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.COLLECTION) {
            userFootDO.setCollectionStat(CollectionStatEnum.COLLECTION.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.CANCEL_COLLECTION) {
            userFootDO.setCollectionStat(CollectionStatEnum.CANCEL_COLLECTION.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.COMMENT) {
            userFootDO.setCommentStat(CommentStatEnum.COMMENT.getCode());
        } else if (operateTypeEnum == OperateTypeEnum.DELETE_COMMENT) {
            userFootDO.setCommentStat(CommentStatEnum.CANCEL_COMMENT.getCode());
        }
        return userFootDO;
    }

    @Override
    public ArticleFootCountDTO queryArticleCountByArticleId(Long articleId) {
        ArticleFootCountDTO res = userFootMapper.queryCountByArticle(articleId);
        if (res == null) {
            res = new ArticleFootCountDTO();
        }
        return res;
    }

    @Override
    public ArticleFootCountDTO queryArticleCountByUserId(Long userId) {
        return userFootMapper.queryArticleFootCount(userId);
    }

    @Override
    public Long queryCommentPraiseCount(Long commentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDocumentId, commentId)
                .eq(UserFootDO::getDocumentType, DocumentTypeEnum.COMMENT.getCode())
                .eq(UserFootDO::getPraiseStat, PraiseStatEnum.PRAISE.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public List<ArticleDO> queryReadArticleList(Long userId, PageParam pageParam) {
        return userFootMapper.queryReadArticleList(userId, pageParam);
    }

    @Override
    public List<ArticleDO> queryCollectionArticleList(Long userId, PageParam pageParam) {
        return userFootMapper.queryCollectionArticleList(userId, pageParam);
    }

    @Override
    public void saveCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor) {
        // 保存文章对应的评论足迹
        doSaveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleAuthor, comment.getArticleId(), comment.getUserId(), OperateTypeEnum.COMMENT);
        // 如果是子评论，则找到父评论的记录，然后设置为已评
        if (parentCommentAuthor != null) {
            doSaveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, parentCommentAuthor, comment.getParentCommentId(), comment.getUserId(), OperateTypeEnum.COMMENT);
        }
    }

    @Override
    public void deleteCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor) {
        doSaveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleAuthor, comment.getArticleId(), comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        if (parentCommentAuthor != null) {
            doSaveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, parentCommentAuthor, comment.getParentCommentId(), comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        }
    }
}
