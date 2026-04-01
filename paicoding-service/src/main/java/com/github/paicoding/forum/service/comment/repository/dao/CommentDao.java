package com.github.paicoding.forum.service.comment.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.SearchCommentReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.CommentAdminDTO;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class CommentDao extends ServiceImpl<CommentMapper, CommentDO> {
    /**
     * 获取划线评论
     *
     * @param articleId
     * @return
     */
    public List<CommentDO> listHighlightCommentList(Long articleId) {
        return lambdaQuery()
                .eq(CommentDO::getTopCommentId, 0)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .isNotNull(CommentDO::getHighlightInfo)
                .list();
    }

    /**
     * 获取评论列表
     *
     * @param pageParam
     * @return
     */
    public List<CommentDO> listTopCommentList(Long articleId, PageParam pageParam) {
        return lambdaQuery()
                .eq(CommentDO::getTopCommentId, 0)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(CommentDO::getId).list();
    }

    /**
     * 查询所有的子评论
     *
     * @param articleId
     * @return
     */
    public List<CommentDO> listSubCommentIdMappers(Long articleId, Collection<Long> topCommentIds) {
        return lambdaQuery()
                .in(CommentDO::getTopCommentId, topCommentIds)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode()).list();
    }


    /**
     * 查询有效评论数
     *
     * @param articleId
     * @return
     */
    public int commentCount(Long articleId) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode());
        return baseMapper.selectCount(queryWrapper).intValue();
    }

    public int topCommentCount(Long articleId) {
        return lambdaQuery()
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getTopCommentId, 0)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count()
                .intValue();
    }

    public CommentDO getHotComment(Long articleId) {
        Map<String, Object> map = baseMapper.getHotTopCommentId(articleId);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        CommentDO hotComment = baseMapper.selectById(Long.parseLong(String.valueOf(map.get("top_comment_id"))));
        if (hotComment == null || hotComment.getDeleted() == YesOrNoEnum.YES.getCode()) {
            return null;
        }
        return hotComment;
    }

    public List<CommentAdminDTO> listCommentsByParams(SearchCommentReq req, PageParam pageParam) {
        return baseMapper.listCommentsByParams(req, pageParam);
    }

    public Long countCommentsByParams(SearchCommentReq req) {
        return baseMapper.countCommentsByParams(req);
    }

    public Long countReplyByTopCommentId(Long topCommentId) {
        return lambdaQuery()
                .eq(CommentDO::getTopCommentId, topCommentId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count();
    }

    /**
     * 批量统计子评论数量
     *
     * @param articleId 文章ID
     * @param topCommentIds 一级评论ID集合
     * @return Map<topCommentId, count>
     */
    public Map<Long, Integer> countSubComments(Long articleId, Collection<Long> topCommentIds) {
        if (CollectionUtils.isEmpty(topCommentIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = baseMapper.countSubCommentsByTopIds(articleId, topCommentIds);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row.get("key")).longValue(),
                row -> ((Number) row.get("value")).intValue()
        ));
    }

    public List<CommentDO> listFirstSubComments(Long articleId, Collection<Long> topCommentIds) {
        if (CollectionUtils.isEmpty(topCommentIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listFirstSubCommentsByTopIds(articleId, topCommentIds);
    }

    /**
     * 分页查询子评论
     *
     * @param topCommentId 一级评论ID
     * @param pageParam 分页参数
     * @return 子评论列表
     */
    public List<CommentDO> listSubComments(Long topCommentId, PageParam pageParam) {
        return lambdaQuery()
                .eq(CommentDO::getTopCommentId, topCommentId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .orderByAsc(CommentDO::getId)
                .list();
    }

}
