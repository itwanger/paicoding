package com.github.paicoding.forum.service.article.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.dto.*;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.*;
import com.github.paicoding.forum.service.article.repository.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/** Publication pointers are the only reader-facing source of truth. All snapshots are append-only. */
@Service
public class ArticleEditionService {
    @Resource private ArticleMapper articles;
    @Resource private ArticleDetailMapper details;
    @Resource private ArticleEditionMapper editions;
    @Resource private com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper comments;

    private void require(boolean condition, String message) {
        if (!condition) throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, message);
    }

    public List<ArticleEditionSummaryDTO> list(Long articleId, boolean admin) {
        return editions.selectList(Wrappers.<ArticleEditionDO>lambdaQuery()
                .eq(ArticleEditionDO::getArticleId, articleId).eq(ArticleEditionDO::getDeleted, 0)
                .eq(!admin, ArticleEditionDO::getState, 1).orderByDesc(ArticleEditionDO::getEditionNo))
                .stream().map(e -> {
                    ArticleEditionSummaryDTO dto = new ArticleEditionSummaryDTO();
                    BeanUtils.copyProperties(e, dto);
                    if (!admin) { dto.setDraftDetailId(null); dto.setDraftStatus(null); }
                    return dto;
                }).collect(Collectors.toList());
    }

    public ArticleReadingContextDTO resolve(ArticleDO article) {
        ArticleEditionRequest request = ArticleEditionRequest.forArticle(article.getId());
        return resolve(article, request == null ? null : request.getEditionId());
    }

    public ArticleReadingContextDTO resolve(ArticleDO article, Long selectedId) {
        ArticleReadingContextDTO context = new ArticleReadingContextDTO();
        context.setArticleId(article.getId());
        context.setVersioned(article.getCurrentEditionId() != null);
        context.setEditions(Collections.emptyList());
        if (!context.isVersioned()) return context;
        if (!Objects.equals(article.getStatus(), 1)) {
            com.github.paicoding.forum.api.model.context.ReqInfoContext.ReqInfo req = com.github.paicoding.forum.api.model.context.ReqInfoContext.getReqInfo();
            boolean editor = req != null && req.getUser() != null && (Objects.equals(req.getUserId(), article.getUserId())
                    || com.github.paicoding.forum.core.permission.UserRole.hasAdminPermission(req.getUser().getRole()));
            require(editor, "文章尚未公开");
        }
        ArticleEditionRequest incoming = ArticleEditionRequest.forArticle(article.getId());
        if (incoming != null) require(Objects.equals(article.getStatus(), 1), "文章尚未公开");
        List<ArticleEditionSummaryDTO> published = list(article.getId(), false);
        Long target = selectedId == null ? article.getCurrentEditionId() : selectedId;
        ArticleEditionSummaryDTO selected = published.stream().filter(e -> Objects.equals(e.getId(), target)).findFirst().orElse(null);
        require(selected != null, "目标版本不存在或已撤回，请刷新页面");
        publicSnapshot(article.getId(), selected.getId(), selected.getPublishedDetailId());
        context.setCurrentEditionId(article.getCurrentEditionId());
        context.setSelectedEditionId(selected.getId());
        context.setSelectedEditionCode(selected.getEditionCode());
        context.setSelectedEditionName(selected.getEditionName());
        context.setSelectedDetailId(selected.getPublishedDetailId());
        context.setViewingHistoryEdition(!Objects.equals(target, article.getCurrentEditionId()));
        context.setEditions(published);
        return context;
    }

    /** Older *published* revisions remain readable as comment context, never drafts. */
    public ArticleDetailDO publicSnapshot(Long articleId, Long editionId, Long detailId) {
        ArticleEditionDO edition = editions.selectById(editionId);
        require(edition != null && Objects.equals(edition.getArticleId(), articleId)
                && Objects.equals(edition.getState(), 1) && Objects.equals(edition.getDeleted(), 0), "版本尚未公开或已撤回");
        ArticleDetailDO detail = detailId == null ? null : details.selectById(detailId);
        require(detail != null && Objects.equals(detail.getArticleId(), articleId)
                && Objects.equals(detail.getEditionId(), editionId) && detail.getFirstPublishedAt() != null
                && Objects.equals(detail.getDeleted(), 0), "正文快照不是公开版本");
        return detail;
    }

    public void apply(ArticleDTO dto, ArticleReadingContextDTO context) {
        ArticleEditionRequest selection = ArticleEditionRequest.forArticle(dto.getArticleId());
        context.setDiscussionScope(selection == null ? "current" : selection.getScope());
        dto.setReadingContext(context);
        if (context.isVersioned()) {
            context.setCurrentCommentCount(comments.selectCount(Wrappers.<com.github.paicoding.forum.service.comment.repository.entity.CommentDO>lambdaQuery()
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getArticleId, dto.getArticleId())
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getTopCommentId, 0)
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getDeleted, 0)
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getEditionId, context.getSelectedEditionId())).intValue());
            List<Long> history = context.getEditions().stream().map(ArticleEditionSummaryDTO::getId).filter(id -> !id.equals(context.getSelectedEditionId())).collect(Collectors.toList());
            context.setHistoryCommentCount(comments.selectCount(Wrappers.<com.github.paicoding.forum.service.comment.repository.entity.CommentDO>lambdaQuery()
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getArticleId, dto.getArticleId())
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getTopCommentId, 0)
                .eq(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getDeleted, 0)
                .and(q -> { q.isNull(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getEditionId); if (!history.isEmpty()) q.or().in(com.github.paicoding.forum.service.comment.repository.entity.CommentDO::getEditionId, history); })).intValue());
        }
        if (!context.isVersioned()) return;
        ArticleDetailDO detail = publicSnapshot(dto.getArticleId(), context.getSelectedEditionId(), context.getSelectedDetailId());
        applyDetail(dto, detail);
    }

    private void applyDetail(ArticleDTO dto, ArticleDetailDO detail) {
        dto.setContent(detail.getContent());
        if (detail.getMetadataSnapshot() != null) {
            ArticleMetadataSnapshotDTO meta = JsonUtil.toObj(detail.getMetadataSnapshot(), ArticleMetadataSnapshotDTO.class);
            dto.setTitle(meta.getTitle()); dto.setShortTitle(meta.getShortTitle());
            dto.setSummary(meta.getSummary()); dto.setCover(meta.getCover());
        }
    }

    /** Admin-only controller calls this; no reader API accepts draft IDs. */
    public ArticleDTO edit(ArticleDTO dto, Long editionId) {
        ArticleEditionDO edition = owned(dto.getArticleId(), editionId);
        Long id = edition.getDraftDetailId() == null ? edition.getPublishedDetailId() : edition.getDraftDetailId();
        ArticleDetailDO detail = details.selectById(id);
        require(detail != null && Objects.equals(detail.getEditionId(), editionId), "版本草稿不存在");
        applyDetail(dto, detail);
        ArticleEditionSummaryDTO editing = new ArticleEditionSummaryDTO();
        BeanUtils.copyProperties(edition, editing);
        if (detail.getMetadataSnapshot() != null) {
            ArticleMetadataSnapshotDTO meta = JsonUtil.toObj(detail.getMetadataSnapshot(), ArticleMetadataSnapshotDTO.class);
            if (meta.getEditionName() != null) editing.setEditionName(meta.getEditionName());
            if (meta.getAudienceTag() != null) editing.setAudienceTag(meta.getAudienceTag());
            if (meta.getGuideTip() != null) editing.setGuideTip(meta.getGuideTip());
            if (meta.getChangeNote() != null) editing.setChangeNote(meta.getChangeNote());
        }
        dto.setEditingEdition(editing);
        return dto;
    }

    private ArticleEditionDO owned(Long articleId, Long editionId) {
        ArticleEditionDO edition = editionId == null ? null : editions.selectById(editionId);
        require(edition != null && Objects.equals(edition.getArticleId(), articleId)
                && Objects.equals(edition.getDeleted(), 0), "版本不属于这篇文章");
        return edition;
    }

    private ArticleDO lock(Long articleId) {
        require(articleId != null, "请先发布文章，再创建正式版本");
        ArticleDO article = articles.selectForUpdateById(articleId);
        require(article != null && Objects.equals(article.getDeleted(), 0), "文章不存在");
        return article;
    }

    private ArticleDetailDO latest(Long id) {
        return details.selectOne(Wrappers.<ArticleDetailDO>lambdaQuery().eq(ArticleDetailDO::getArticleId, id)
                .orderByDesc(ArticleDetailDO::getVersion).last("limit 1"));
    }

    private String metadata(String title, String shortTitle, String summary, String cover) {
        ArticleMetadataSnapshotDTO dto = new ArticleMetadataSnapshotDTO();
        dto.setTitle(title); dto.setShortTitle(shortTitle == null ? "" : shortTitle); dto.setSummary(summary == null ? "" : summary); dto.setCover(cover == null ? "" : cover);
        return JsonUtil.toStr(dto);
    }

    private ArticleDetailDO append(ArticleDO article, ArticleEditionDO edition, String content, String metadata, boolean publish) {
        ArticleDetailDO previous = latest(article.getId());
        ArticleDetailDO snapshot = new ArticleDetailDO();
        snapshot.setArticleId(article.getId()); snapshot.setEditionId(edition.getId());
        snapshot.setVersion(previous == null ? 1L : previous.getVersion() + 1);
        snapshot.setContent(content); snapshot.setMetadataSnapshot(metadata); snapshot.setDeleted(0);
        Date now = new Date(); snapshot.setCreateTime(now); snapshot.setUpdateTime(now);
        if (publish) snapshot.setFirstPublishedAt(now);
        details.insert(snapshot);
        return snapshot;
    }

    private ArticleEditionDO create(ArticleDO article, String code, String name, String audience, String guide, String note) {
        require(code != null && code.matches("[a-z0-9][a-z0-9-]{0,31}"), "版本编码仅支持小写字母、数字、连字符，最长32位");
        require(name != null && !name.trim().isEmpty() && name.length() <= 80, "请填写80字以内的版本名称");
        require(audience == null || audience.length() <= 120, "受众说明最长120字");
        require(guide == null || guide.length() <= 500, "引导语最长500字");
        require(editions.selectCount(Wrappers.<ArticleEditionDO>lambdaQuery().eq(ArticleEditionDO::getArticleId, article.getId())
                .eq(ArticleEditionDO::getEditionCode, code)) == 0, "版本编码已存在");
        ArticleEditionDO last = editions.selectOne(Wrappers.<ArticleEditionDO>lambdaQuery().eq(ArticleEditionDO::getArticleId, article.getId())
                .orderByDesc(ArticleEditionDO::getEditionNo).last("limit 1"));
        ArticleEditionDO edition = new ArticleEditionDO();
        edition.setArticleId(article.getId()); edition.setEditionNo(last == null ? 1 : last.getEditionNo() + 1);
        edition.setEditionCode(code); edition.setEditionName(name.trim()); edition.setAudienceTag(audience == null ? "" : audience);
        edition.setGuideTip(guide == null ? "" : guide); edition.setChangeNote(note);
        edition.setState(0); edition.setDeleted(0); edition.setDraftStatus(0);
        editions.insert(edition);
        return edition;
    }

    private void publish(ArticleEditionDO edition, ArticleDetailDO detail) {
        Date now = new Date();
        edition.setPublishedDetailId(detail.getId()); edition.setState(1); edition.setDraftStatus(0);
        if (edition.getFirstPublishedAt() == null) edition.setFirstPublishedAt(now);
        edition.setLastPublishedAt(now);
        editions.updateById(edition);
        // MyBatis' NOT_NULL update strategy cannot clear a pointer with updateById.
        editions.update(null, Wrappers.<ArticleEditionDO>lambdaUpdate().eq(ArticleEditionDO::getId, edition.getId())
                .set(ArticleEditionDO::getDraftDetailId, null));
    }

    private void makeDefault(ArticleDO article, ArticleEditionDO edition) {
        ArticleDetailDO detail = publicSnapshot(article.getId(), edition.getId(), edition.getPublishedDetailId());
        ArticleMetadataSnapshotDTO meta = JsonUtil.toObj(detail.getMetadataSnapshot(), ArticleMetadataSnapshotDTO.class);
        article.setCurrentEditionId(edition.getId()); article.setTitle(meta.getTitle()); article.setShortTitle(meta.getShortTitle());
        article.setSummary(meta.getSummary()); article.setPicture(meta.getCover()); article.setUpdateTime(new Date());
        articles.updateById(article);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleEditionSummaryDTO save(ArticlePostReq req) {
        ArticleDO article = lock(req.getArticleId());
        require(Objects.equals(article.getStatus(), 1), "请先将文章上线，再管理正式版本");
        require(req.getContent() != null && !req.getContent().trim().isEmpty(), "正文不能为空");
        require(req.getTitle() != null && !req.getTitle().trim().isEmpty(), "标题不能为空");
        require(req.getTitle().length() <= 120, "标题最长120字");
        require(req.getShortTitle() == null || req.getShortTitle().length() <= 120, "短标题最长120字");
        require(req.getSummary() == null || req.getSummary().length() <= 300, "摘要最长300字");
        String mode = req.getPublishMode();
        require(Arrays.asList("NEW_EDITION", "REVISE_EDITION", "SAVE_DRAFT").contains(mode), "请选择版本发布方式");
        boolean draft = "SAVE_DRAFT".equals(mode);
        if (article.getCurrentEditionId() == null) {
            // Archive exactly what is online. Do not guess an original edition or migrate old comments.
            ArticleDetailDO source = details.selectOne(Wrappers.<ArticleDetailDO>lambdaQuery()
                    .eq(ArticleDetailDO::getArticleId, article.getId()).eq(ArticleDetailDO::getDeleted, 0)
                    .orderByDesc(ArticleDetailDO::getVersion).last("limit 1"));
            require(source != null && Objects.equals(source.getId(), req.getExpectedPublishedDetailId()), "线上正文已变化，请重新加载后再启用版本管理");
            ArticleEditionDO baseline = create(article, "v1", "启用时存档", "", "此版保存启用版本管理时的线上正文。", "原有留言保留在早期讨论");
            publish(baseline, append(article, baseline, source.getContent(), metadata(article.getTitle(), article.getShortTitle(), article.getSummary(), article.getPicture()), true));
            article.setCurrentEditionId(baseline.getId());
            articles.update(null, Wrappers.<ArticleDO>lambdaUpdate().eq(ArticleDO::getId, article.getId())
                    .set(ArticleDO::getCurrentEditionId, baseline.getId()).set(ArticleDO::getUpdateTime, article.getUpdateTime()));
        }
        ArticleEditionDO edition;
        if (req.getEditionId() == null || "NEW_EDITION".equals(mode)) {
            edition = create(article, req.getEditionCode(), req.getEditionName(), req.getAudienceTag(), req.getGuideTip(), req.getChangeNote());
        } else {
            edition = owned(article.getId(), req.getEditionId());
            require(Objects.equals(edition.getPublishedDetailId(), req.getExpectedPublishedDetailId())
                    && Objects.equals(edition.getDraftDetailId(), req.getExpectedDraftDetailId()), "这个版本已被更新，请先重新加载，避免覆盖他人的修改");
            if (req.getEditionName() != null) {
                require(!req.getEditionName().trim().isEmpty() && req.getEditionName().length() <= 80, "版本名称最长80字");
                // Published labels stay stable while saving drafts. Body metadata is stored in the snapshot.
                if (!draft) edition.setEditionName(req.getEditionName());
            }
            if (!draft) {
                require(req.getAudienceTag() == null || req.getAudienceTag().length() <= 120, "受众说明最长120字");
                require(req.getGuideTip() == null || req.getGuideTip().length() <= 500, "引导语最长500字");
                if (req.getAudienceTag() != null) edition.setAudienceTag(req.getAudienceTag());
                if (req.getGuideTip() != null) edition.setGuideTip(req.getGuideTip());
                if (req.getChangeNote() != null) edition.setChangeNote(req.getChangeNote());
            }
        }
        ArticleMetadataSnapshotDTO meta = JsonUtil.toObj(metadata(req.getTitle(), req.getShortTitle(), req.getSummary(), req.getCover()), ArticleMetadataSnapshotDTO.class);
        require(req.getAudienceTag() == null || req.getAudienceTag().length() <= 120, "受众说明最长120字");
        require(req.getGuideTip() == null || req.getGuideTip().length() <= 500, "引导语最长500字");
        meta.setEditionName(req.getEditionName()); meta.setAudienceTag(req.getAudienceTag());
        meta.setGuideTip(req.getGuideTip()); meta.setChangeNote(req.getChangeNote());
        ArticleDetailDO snapshot = append(article, edition, req.getContent(), JsonUtil.toStr(meta), !draft);
        if (draft) {
            edition.setDraftDetailId(snapshot.getId()); edition.setDraftStatus(1); editions.updateById(edition);
        } else {
            publish(edition, snapshot);
            if (Boolean.TRUE.equals(req.getSetAsDefault()) || Objects.equals(article.getCurrentEditionId(), edition.getId())) makeDefault(article, edition);
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.EDITION_UPDATED, article));
        }
        ArticleEditionSummaryDTO result = new ArticleEditionSummaryDTO(); BeanUtils.copyProperties(edition, result);
        if (!draft) result.setDraftDetailId(null);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long articleId, Long editionId) {
        ArticleDO article = lock(articleId); ArticleEditionDO edition = owned(articleId, editionId);
        makeDefault(article, edition);
        SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.EDITION_UPDATED, article));
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long articleId, Long editionId, Long fallbackId) {
        ArticleDO article = lock(articleId); ArticleEditionDO edition = owned(articleId, editionId);
        if (Objects.equals(article.getCurrentEditionId(), editionId)) {
            require(fallbackId != null && !Objects.equals(fallbackId, editionId), "撤回默认版前请选择另一个已公开版本");
            makeDefault(article, owned(articleId, fallbackId));
        }
        edition.setState(2); editions.updateById(edition);
        SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.EDITION_UPDATED, article));
    }
}
