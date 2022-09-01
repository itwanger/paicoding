package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.PraiseStatEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.CommentService;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.user.UserFootService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Autowired
    private CommentService commentService;

    /**
     * 保存或更新状态信息
     *
     * @param documentType    文档类型：博文 + 评论
     * @param documentId      文档id
     * @param authorId        作者
     * @param userId          操作人
     * @param operateTypeEnum 操作类型：点赞，评论，收藏等
     */
    @Override
    public UserFootDO saveOrUpdateUserFoot(DocumentTypeEnum documentType, Long documentId, Long authorId, Long userId, OperateTypeEnum operateTypeEnum) {
        // 查询是否有该足迹；有则更新，没有则插入
        UserFootDO readUserFootDO = userFootMapper.queryFootByDocumentInfo(documentId, documentType.getCode(), userId);
        if (readUserFootDO == null) {
            readUserFootDO = new UserFootDO();
            readUserFootDO.setUserId(userId);
            readUserFootDO.setDocumentId(documentId);
            readUserFootDO.setDocumentType(documentType.getCode());
            readUserFootDO.setDocumentUserId(authorId);
            setUserFootStat(readUserFootDO, operateTypeEnum);
            userFootMapper.insert(readUserFootDO);
        } else if (setUserFootStat(readUserFootDO, operateTypeEnum)) {
            userFootMapper.updateById(readUserFootDO);
        }
        return readUserFootDO;
    }

    private boolean setUserFootStat(UserFootDO userFootDO, OperateTypeEnum operate) {
        switch (operate) {
            case READ:
                return compareAndUpdate(userFootDO::getReadStat, userFootDO::setReadStat, operate.getDbStatCode());
            case PRAISE:
            case CANCEL_PRAISE:
                return compareAndUpdate(userFootDO::getPraiseStat, userFootDO::setPraiseStat, operate.getDbStatCode());
            case COLLECTION:
            case CANCEL_COLLECTION:
                return compareAndUpdate(userFootDO::getCollectionStat, userFootDO::setCollectionStat, operate.getDbStatCode());
            case COMMENT:
            case DELETE_COMMENT:
                return compareAndUpdate(userFootDO::getCommentStat, userFootDO::setCommentStat, operate.getDbStatCode());
            default:
                return false;
        }
    }

    /**
     * 相同则直接返回false不用更新；不同则更新,返回true
     *
     * @param supplier
     * @param consumer
     * @param input
     * @param <T>
     * @return
     */
    private <T> boolean compareAndUpdate(Supplier<T> supplier, Consumer<T> consumer, T input) {
        if (Objects.equals(supplier.get(), input)) {
            return false;
        }
        consumer.accept(input);
        return true;
    }

    @Override
    public ArticleFootCountDTO queryArticleCountByArticleId(Long articleId) {
        ArticleFootCountDTO res = userFootMapper.queryCountByArticle(articleId);
        if (res == null) {
            res = new ArticleFootCountDTO();
        } else {
            res.setCommentCount(commentService.commentCount(articleId));
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
        saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleAuthor, comment.getArticleId(), comment.getUserId(), OperateTypeEnum.COMMENT);
        // 如果是子评论，则找到父评论的记录，然后设置为已评
        if (parentCommentAuthor != null) {
            saveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, parentCommentAuthor, comment.getParentCommentId(), comment.getUserId(), OperateTypeEnum.COMMENT);
        }
    }

    @Override
    public void deleteCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor) {
        saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleAuthor, comment.getArticleId(), comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        if (parentCommentAuthor != null) {
            saveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, parentCommentAuthor, comment.getParentCommentId(), comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        }
    }
}
