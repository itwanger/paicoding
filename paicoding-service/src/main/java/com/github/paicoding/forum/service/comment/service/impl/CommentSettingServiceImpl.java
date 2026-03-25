package com.github.paicoding.forum.service.comment.service.impl;

import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.SearchCommentReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.CommentAdminDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.comment.repository.dao.CommentDao;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentSettingService;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import com.github.paicoding.forum.service.sensitive.service.SensitiveBypassService;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentSettingServiceImpl implements CommentSettingService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private CountService countService;

    @Autowired
    private CommentWriteService commentWriteService;

    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private SensitiveBypassService sensitiveBypassService;

    @Override
    public PageVo<CommentAdminDTO> getCommentList(SearchCommentReq req) {
        long pageNumber = req.getPageNumber() <= 0 ? PageParam.DEFAULT_PAGE_NUM : req.getPageNumber();
        long pageSize = req.getPageSize() <= 0 ? PageParam.DEFAULT_PAGE_SIZE : req.getPageSize();
        if (StringUtils.isBlank(req.getContent())) {
            PageParam pageParam = PageParam.newPageInstance(pageNumber, pageSize);
            return PageVo.build(commentDao.listCommentsByParams(req, pageParam), pageSize, pageNumber, commentDao.countCommentsByParams(req));
        }

        List<CommentAdminDTO> matched = commentDao.listCommentsByParams(req, null).stream()
                .filter(comment -> matchContent(req.getContent(), comment))
                .collect(Collectors.toList());
        int fromIndex = (int) Math.min((pageNumber - 1) * pageSize, matched.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, matched.size());
        List<CommentAdminDTO> pageList = fromIndex >= toIndex ? Collections.emptyList() : matched.subList(fromIndex, toIndex);
        return PageVo.build(pageList, pageSize, pageNumber, matched.size());
    }

    @Override
    public CommentAdminDTO getCommentDetail(Long commentId) {
        CommentDO comment = commentDao.getById(commentId);
        if (comment == null || comment.getDeleted() == YesOrNoEnum.YES.getCode()) {
            throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "评论ID=" + commentId);
        }

        CommentAdminDTO dto = new CommentAdminDTO();
        dto.setCommentId(comment.getId());
        dto.setArticleId(comment.getArticleId());
        dto.setUserId(comment.getUserId());
        dto.setCommentContent(comment.getContent());
        dto.setParentCommentId(comment.getParentCommentId());
        dto.setTopCommentId(comment.getTopCommentId());
        dto.setHighlightInfo(comment.getHighlightInfo());
        dto.setCommentType(NumUtil.nullOrZero(comment.getTopCommentId()) ? 1 : 2);
        dto.setCreateTime(comment.getCreateTime());
        dto.setUpdateTime(comment.getUpdateTime());
        dto.setPraiseCount(countService.queryCommentPraiseCount(commentId));
        dto.setReplyCount(NumUtil.nullOrZero(comment.getTopCommentId()) ? commentDao.countReplyByTopCommentId(commentId) : 0L);

        ArticleDO article = articleDao.getById(comment.getArticleId());
        if (article != null) {
            dto.setArticleTitle(article.getTitle());
        }

        BaseUserInfoDTO userInfo = safeQueryUser(comment.getUserId());
        if (userInfo != null) {
            dto.setUserName(userInfo.getUserName());
            dto.setUserAvatar(userInfo.getPhoto());
        }

        if (NumUtil.upZero(comment.getParentCommentId())) {
            CommentDO parentComment = commentDao.getById(comment.getParentCommentId());
            if (parentComment != null) {
                dto.setParentCommentContent(parentComment.getContent());
            }
        }
        if (NumUtil.upZero(comment.getTopCommentId())) {
            CommentDO topComment = commentDao.getById(comment.getTopCommentId());
            if (topComment != null) {
                dto.setTopCommentContent(topComment.getContent());
            }
        }
        return dto;
    }

    @Override
    public Long saveComment(CommentSaveReq req, Long operateUserId) {
        String content = StringUtils.trimToEmpty(req.getCommentContent());
        if (StringUtils.isBlank(content)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论内容不能为空");
        }
        req.setCommentContent(content);

        if (NumUtil.nullOrZero(req.getCommentId())) {
            if (NumUtil.nullOrZero(req.getArticleId())) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章ID不能为空");
            }
            ArticleDO article = articleDao.getById(req.getArticleId());
            if (article == null || article.getDeleted() == YesOrNoEnum.YES.getCode()) {
                throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, req.getArticleId());
            }

            if (NumUtil.upZero(req.getParentCommentId())) {
                CommentDO parentComment = commentDao.getById(req.getParentCommentId());
                if (parentComment == null || parentComment.getDeleted() == YesOrNoEnum.YES.getCode()) {
                    throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "父评论=" + req.getParentCommentId());
                }
                if (!req.getArticleId().equals(parentComment.getArticleId())) {
                    throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回复的评论不属于当前文章");
                }
                req.setTopCommentId(NumUtil.upZero(parentComment.getTopCommentId()) ? parentComment.getTopCommentId() : parentComment.getId());
            } else {
                req.setParentCommentId(0L);
                req.setTopCommentId(0L);
            }
            req.setUserId(operateUserId);
        } else {
            CommentDO origin = commentDao.getById(req.getCommentId());
            if (origin == null || origin.getDeleted() == YesOrNoEnum.YES.getCode()) {
                throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "评论ID=" + req.getCommentId());
            }
        }
        return commentWriteService.saveComment(req);
    }

    @Override
    public void deleteComment(Long commentId) {
        CommentDO comment = commentDao.getById(commentId);
        if (comment == null || comment.getDeleted() == YesOrNoEnum.YES.getCode()) {
            throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "评论ID=" + commentId);
        }
        commentWriteService.deleteComment(commentId, comment.getUserId());
    }

    private BaseUserInfoDTO safeQueryUser(Long userId) {
        try {
            return userService.queryBasicUserInfo(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean matchContent(String keyword, CommentAdminDTO comment) {
        String normalizedKeyword = normalizeSearchText(keyword);
        if (StringUtils.isBlank(normalizedKeyword)) {
            return true;
        }

        String originalContent = StringUtils.defaultString(comment.getCommentContent());
        if (normalizeSearchText(originalContent).contains(normalizedKeyword)) {
            return true;
        }

        if (!sensitiveBypassService.shouldBypassByUserId(comment.getUserId())) {
            String displayContent = sensitiveService.replace(originalContent);
            return normalizeSearchText(displayContent).contains(normalizedKeyword);
        }
        return false;
    }

    private String normalizeSearchText(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase();
        normalized = normalized.replaceAll("\\s+", "");
        return normalized.trim();
    }
}
