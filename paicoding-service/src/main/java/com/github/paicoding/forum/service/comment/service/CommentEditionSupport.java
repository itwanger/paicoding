package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleReadingContextDTO;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleMapper;
import com.github.paicoding.forum.service.article.service.ArticleEditionService;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Objects;

/** Threads inherit their root's immutable reading context, including bot replies. */
@Service
public class CommentEditionSupport {
    @Resource private ArticleMapper articles;
    @Resource private CommentMapper comments;
    @Resource private com.github.paicoding.forum.service.article.repository.mapper.ArticleDetailMapper details;
    @Resource private ArticleEditionService editions;
    @Resource private com.github.paicoding.forum.service.article.service.ArticleReadingAccess readingAccess;

    private void require(boolean condition, String message) {
        if (!condition) throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, message);
    }
    public ArticleReadingContextDTO context(Long articleId) {
        ArticleDO article = articles.selectById(articleId);
        require(article != null && Objects.equals(article.getDeleted(), 0), "文章不存在");
        if (article.getCurrentEditionId() != null) require(Objects.equals(article.getStatus(), 1), "文章尚未公开");
        return editions.resolve(article);
    }
    public CommentDO root(Long id) {
        CommentDO comment = comments.selectById(id);
        require(comment != null && Objects.equals(comment.getDeleted(), 0), "评论不存在");
        if (comment.getTopCommentId() != null && comment.getTopCommentId() > 0) {
            CommentDO root = comments.selectById(comment.getTopCommentId());
            require(root != null && Objects.equals(root.getDeleted(), 0) && Objects.equals(root.getArticleId(), comment.getArticleId()), "评论线程不存在");
            comment = root;
        }
        return comment;
    }
    public void requireVisible(CommentDO root) {
        ArticleDO article = articles.selectById(root.getArticleId());
        require(article != null && Objects.equals(article.getDeleted(), 0) && Objects.equals(article.getStatus(), 1), "文章尚未公开");
        if (root.getEditionId() != null) editions.publicSnapshot(root.getArticleId(), root.getEditionId(), root.getArticleDetailId());
    }
    public void prepare(CommentSaveReq req) {
        if (req.getParentCommentId() != null && req.getParentCommentId() > 0) {
            CommentDO root = root(req.getParentCommentId());
            require(Objects.equals(root.getArticleId(), req.getArticleId()), "不能跨文章回复评论");
            require(req.getTopCommentId() == null || req.getTopCommentId() == 0 || Objects.equals(req.getTopCommentId(), root.getId()), "回复的顶层评论不匹配");
            requireVisible(root);
            req.setTopCommentId(root.getId()); req.setEditionId(root.getEditionId()); req.setArticleDetailId(root.getArticleDetailId());
            return;
        }
        require(req.getTopCommentId() == null || req.getTopCommentId() == 0, "顶层评论不能指定其他线程");
        ArticleReadingContextDTO context = context(req.getArticleId());
        if (!context.isVersioned()) { req.setEditionId(null); req.setArticleDetailId(null); return; }
        ArticleEditionRequest selected = ArticleEditionRequest.forArticle(req.getArticleId());
        if (req.getEditionId() == null && selected != null) req.setEditionId(selected.getEditionId());
        if (req.getArticleDetailId() == null && selected != null) req.setArticleDetailId(selected.getDetailId());
        require(req.getEditionId() != null && req.getArticleDetailId() != null, "文章已更新，请刷新后再评论");
        editions.publicSnapshot(req.getArticleId(), req.getEditionId(), req.getArticleDetailId());
    }
    public long replyPage(Long rootId, Long commentId) {
        long offset = comments.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<CommentDO>lambdaQuery()
                .eq(CommentDO::getTopCommentId, rootId).eq(CommentDO::getDeleted, 0).lt(CommentDO::getId, commentId));
        return offset / 10 + 1;
    }
    public String aiContext(Long commentId) {
        CommentDO root = root(commentId); requireVisible(root);
        CommentDO question = comments.selectById(commentId);
        if (!readingAccess.canReadFull(articles.selectById(root.getArticleId()), question.getUserId())) {
            return "当前读者仅可试看，请只根据问题和读者提供的引用回答，不提供未解锁的教程正文。";
        }
        if (root.getEditionId() != null) return editions.publicSnapshot(root.getArticleId(), root.getEditionId(), root.getArticleDetailId()).getContent();
        ArticleDO article = articles.selectById(root.getArticleId());
        if (article.getCurrentEditionId() == null) {
            com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO detail = details.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO>lambdaQuery()
                    .eq(com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO::getArticleId, article.getId())
                    .eq(com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO::getDeleted, 0)
                    .orderByDesc(com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO::getVersion).last("limit 1"));
            return detail == null ? "" : detail.getContent();
        }
        // A historical thread with unknown attribution must not silently use today's body.
        return "该线程属于早期讨论，未保留可靠的正文快照。请仅依据用户的问题与引用作答，不假定其属于当前教程版本。";
    }
}
