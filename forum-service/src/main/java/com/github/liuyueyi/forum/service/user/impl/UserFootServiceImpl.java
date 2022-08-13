package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.*;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.repository.ArticleRepository;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.user.UserFootService;
import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;
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

    @Resource
    private ArticleRepository articleRepository;

    @Override
    public ArticleFootCountDTO saveArticleFoot(Long articleId, Long userId, OperateTypeEnum operateTypeEnum) {
        ArticleDTO article = articleRepository.queryArticleDetail(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        // 查询是否有该足迹
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, articleId)
                .eq(UserFootDO::getDoucumentType, DocumentTypeEnum.DOCUMENT.getCode())
                .eq(UserFootDO::getUserId, userId);
        UserFootDO readUserFootDO = userFootMapper.selectOne(query);
        if (readUserFootDO == null) {
            UserFootDO userFootDO = new UserFootDO();
            userFootDO.setUserId(userId);
            userFootDO.setDoucumentId(article.getArticleId());
            userFootDO.setDoucumentType(DocumentTypeEnum.DOCUMENT.getCode());
            userFootDO.setDoucumentUserId(article.getAuthor());
            userFootDO = setUserFootStat(userFootDO, operateTypeEnum);
            userFootMapper.insert(userFootDO);
        } else {
            readUserFootDO = setUserFootStat(readUserFootDO, operateTypeEnum);
            userFootMapper.updateById(readUserFootDO);
        }
        return userFootMapper.queryCountByArticle(articleId);
    }

    private UserFootDO setUserFootStat(UserFootDO userFootDO, OperateTypeEnum operateTypeEnum) {
        if (operateTypeEnum.equals(OperateTypeEnum.READ)) {
            userFootDO.setReadStat(ReadStatEnum.READ.getCode());
        } else if (operateTypeEnum.equals(OperateTypeEnum.PRAISE)) {
            userFootDO.setPraiseStat(PraiseStatEnum.PRAISE.getCode());
        } else if (operateTypeEnum.equals(OperateTypeEnum.COLLECTION)) {
            userFootDO.setCommentStat(CollectionStatEnum.COLLECTION.getCode());
        } else if (operateTypeEnum.equals(OperateTypeEnum.CANCEL_PRAISE)) {
            userFootDO.setPraiseStat(PraiseStatEnum.CANCEL_PRAISE.getCode());
        } else if (operateTypeEnum.equals(OperateTypeEnum.CANCEL_COLLECTION)) {
            userFootDO.setCommentStat(CollectionStatEnum.CANCEL_COLLECTION.getCode());
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
        query.eq(UserFootDO::getDoucumentId, commentId)
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
    public void saveCommentFoot(CommentSaveReq commentSaveReq, Long commentId, Long articleUserId) {

        // 保存评论足迹(针对文章)
        UserFootDO userFootDO = new UserFootDO();
        userFootDO.setUserId(commentSaveReq.getUserId());
        userFootDO.setDoucumentId(commentSaveReq.getArticleId());
        userFootDO.setDoucumentType(DocumentTypeEnum.DOCUMENT.getCode());
        userFootDO.setDoucumentUserId(articleUserId);
        userFootDO.setCommentId(commentId);
        userFootDO.setCommentStat(CommentStatEnum.COMMENT.getCode());
        userFootMapper.insert(userFootDO);

        // 保存评论足迹(针对父评论)
        if (commentSaveReq.getParentCommentId() != null && commentSaveReq.getParentCommentId() != 0) {
            UserFootDO commentUserFootDO = new UserFootDO();
            commentUserFootDO.setUserId(commentSaveReq.getUserId());
            commentUserFootDO.setDoucumentId(commentSaveReq.getParentCommentId());
            commentUserFootDO.setDoucumentType(DocumentTypeEnum.COMMENT.getCode());
            commentUserFootDO.setDoucumentUserId(articleUserId);
            commentUserFootDO.setCommentId(commentId);
            commentUserFootDO.setCommentStat(CommentStatEnum.COMMENT.getCode());
            userFootMapper.insert(commentUserFootDO);
        }
    }

    @Override
    public void deleteCommentFoot(CommentDO commentDO) throws Exception {

        // 删除评论足迹(文章)
        LambdaQueryWrapper<UserFootDO> articleQuery = Wrappers.lambdaQuery();
        articleQuery.eq(UserFootDO::getUserId, commentDO.getUserId()).
                eq(UserFootDO::getDoucumentId, commentDO.getArticleId()).
                eq(UserFootDO::getDoucumentType, DocumentTypeEnum.DOCUMENT.getCode()).
                eq(UserFootDO::getCommentId, commentDO.getId());
        UserFootDO articleUserFootDO = userFootMapper.selectOne(articleQuery);
        if (articleUserFootDO == null) {
            throw new Exception("未查询到该评论足迹");
        }
        articleUserFootDO.setCommentStat(CommentStatEnum.CANCEL_COMMENT.getCode());
        userFootMapper.updateById(articleUserFootDO);

        // 删除评论足迹(父评论)
        if (commentDO.getParentCommentId() != null && commentDO.getParentCommentId() != 0) {
            LambdaQueryWrapper<UserFootDO> commentQuery = Wrappers.lambdaQuery();
            commentQuery.eq(UserFootDO::getUserId, commentDO.getUserId()).
                    eq(UserFootDO::getDoucumentId, commentDO.getParentCommentId()).
                    eq(UserFootDO::getDoucumentType, DocumentTypeEnum.COMMENT.getCode()).
                    eq(UserFootDO::getCommentId, commentDO.getId());
            UserFootDO commentUserFootDO = userFootMapper.selectOne(commentQuery);
            if (commentUserFootDO == null) {
                throw new Exception("未查询到该评论足迹");
            }
            commentUserFootDO.setCommentStat(CommentStatEnum.CANCEL_COMMENT.getCode());
            userFootMapper.updateById(commentUserFootDO);
        }
    }
}
