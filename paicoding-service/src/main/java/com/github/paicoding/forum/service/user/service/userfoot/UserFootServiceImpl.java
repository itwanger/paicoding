package com.github.paicoding.forum.service.user.service.userfoot;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserFootStatisticDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.user.repository.dao.UserFootDao;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserFootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 用户足迹Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class UserFootServiceImpl implements UserFootService {
    private final UserFootDao userFootDao;

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private CommentReadService commentReadService;

    public UserFootServiceImpl(UserFootDao userFootDao) {
        this.userFootDao = userFootDao;
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
    @Override
    public UserFootDO saveOrUpdateUserFoot(DocumentTypeEnum documentType, Long documentId, Long authorId, Long userId, OperateTypeEnum operateTypeEnum) {
        // 查询是否有该足迹；有则更新，没有则插入
        UserFootDO readUserFootDO = userFootDao.getByDocumentAndUserId(documentId, documentType.getCode(), userId);
        if (readUserFootDO == null) {
            readUserFootDO = new UserFootDO();
            readUserFootDO.setUserId(userId);
            readUserFootDO.setDocumentId(documentId);
            readUserFootDO.setDocumentType(documentType.getCode());
            readUserFootDO.setDocumentUserId(authorId);
            setUserFootStat(readUserFootDO, operateTypeEnum);
            userFootDao.save(readUserFootDO);
        } else if (setUserFootStat(readUserFootDO, operateTypeEnum)) {
            readUserFootDO.setUpdateTime(new Date());
            userFootDao.updateById(readUserFootDO);
        }
        return readUserFootDO;
    }


    @Override
    public void saveCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor) {
        // 保存文章对应的评论足迹
        saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, comment.getArticleId(), articleAuthor, comment.getUserId(), OperateTypeEnum.COMMENT);
        // 如果是子评论，则找到父评论的记录，然后设置为已评
        if (comment.getParentCommentId() != null && comment.getParentCommentId() != 0) {
            // 如果需要展示父评论的子评论数量，authorId 需要传父评论的 userId
            saveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, comment.getParentCommentId(), parentCommentAuthor, comment.getUserId(), OperateTypeEnum.COMMENT);
        }
    }

    @Override
    public void removeCommentFoot(CommentDO comment, Long articleAuthor, Long parentCommentAuthor) {
        saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, comment.getArticleId(), articleAuthor, comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        if (comment.getParentCommentId() != null) {
            // 如果需要展示父评论的子评论数量，authorId 需要传父评论的 userId
            saveOrUpdateUserFoot(DocumentTypeEnum.COMMENT, comment.getParentCommentId(), parentCommentAuthor, comment.getUserId(), OperateTypeEnum.DELETE_COMMENT);
        }
    }


    private boolean setUserFootStat(UserFootDO userFootDO, OperateTypeEnum operate) {
        switch (operate) {
            case READ:
                // 设置为已读
                userFootDO.setReadStat(1);
                // 需要更新时间，用于浏览记录
                return true;
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
    public List<Long> queryUserReadArticleList(Long userId, PageParam pageParam) {
        return userFootDao.listReadArticleByUserId(userId, pageParam);
    }

    @Override
    public List<Long> queryUserCollectionArticleList(Long userId, PageParam pageParam) {
        return userFootDao.listCollectedArticlesByUserId(userId, pageParam);
    }

    @Override
    public List<SimpleUserInfoDTO> queryArticlePraisedUsers(Long articleId) {
        return userFootDao.listDocumentPraisedUsers(articleId, DocumentTypeEnum.ARTICLE.getCode(), 10);
    }

    @Override
    public UserFootDO queryUserFoot(Long documentId, Integer type, Long userId) {
        return userFootDao.getByDocumentAndUserId(documentId, type, userId);
    }

    @Override
    public UserFootStatisticDTO getFootCount() {
        return userFootDao.getFootCount();
    }

}
