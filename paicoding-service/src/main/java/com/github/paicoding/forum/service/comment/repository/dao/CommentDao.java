package com.github.paicoding.forum.service.comment.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.repository.mapper.CommentMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class CommentDao extends ServiceImpl<CommentMapper, CommentDO> {

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

    public CommentDO getHotComment(Long articleId) {
        Map<String, Object> map = baseMapper.getHotTopCommentId(articleId);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }

        return baseMapper.selectById(Long.parseLong(String.valueOf(map.get("top_comment_id"))));
    }

}
