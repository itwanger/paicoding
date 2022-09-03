package com.github.liuyueyi.forum.service.comment.service.impl;

import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.converter.CommentConverter;
import com.github.liuyueyi.forum.service.comment.repository.dao.CommentDao;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.comment.service.CommentWriteService;
import com.github.liuyueyi.forum.service.user.service.UserFootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 评论Service
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class CommentWriteServiceImpl implements CommentWriteService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private UserFootService userFootWriteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentSaveReq commentSaveReq) {
        // 保存评论
        CommentDO comment;
        if (NumUtil.nullOrZero(commentSaveReq.getCommentId())) {
            comment = addComment(commentSaveReq);
        } else {
            comment = updateComment(commentSaveReq);
        }
        return comment.getId();
    }

    private CommentDO addComment(CommentSaveReq commentSaveReq) {
        // 0.校验父评论是否存在
        getParentCommentUser(commentSaveReq.getParentCommentId());

        // 1. 保存评论内容
        CommentDO commentDO = CommentConverter.toDo(commentSaveReq);
        Date now = new Date();
        commentDO.setCreateTime(now);
        commentDO.setUpdateTime(now);
        commentDao.save(commentDO);

        // 2. 获取文章信息
        ArticleDTO articleDTO = articleDao.queryArticleDetail(commentSaveReq.getArticleId());
        if (articleDTO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "文章=" + commentSaveReq.getArticleId());
        }

        // 2. 保存足迹信息 : 文章的已评信息 + 评论的已评信息
        userFootWriteService.saveCommentFoot(commentDO, articleDTO);
        return commentDO;
    }

    private CommentDO updateComment(CommentSaveReq commentSaveReq) {
        // 更新评论
        CommentDO commentDO = commentDao.getById(commentSaveReq.getCommentId());
        if (commentDO == null) {
            throw new RuntimeException("未查询到该评论");
        }
        commentDO.setContent(commentSaveReq.getCommentContent());
        commentDao.updateById(commentDO);
        return commentDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        CommentDO commentDO = commentDao.getById(commentId);
        if (commentDO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "评论ID=" + commentId);
        }
        commentDO.setDeleted(YesOrNoEnum.YES.getCode());
        commentDao.updateById(commentDO);

        // 获取文章信息
        ArticleDTO articleDTO = articleDao.queryArticleDetail(commentDO.getArticleId());
        if (articleDTO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "文章=" + commentDO.getArticleId());
        }

        userFootWriteService.removeCommentFoot(commentDO, articleDTO);
    }


    private Long getParentCommentUser(Long parentCommentId) {
        if (NumUtil.nullOrZero(parentCommentId)) {
            return null;

        }
        CommentDO parent = commentDao.getById(parentCommentId);
        if (parent == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "父评论=" + parentCommentId);
        }
        return parent.getUserId();
    }

}
