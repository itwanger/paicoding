package com.github.paicoding.forum.service.comment.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.SearchCommentReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.CommentAdminDTO;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 评论mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface CommentMapper extends BaseMapper<CommentDO> {
    Map<String, Object> getHotTopCommentId(@Param("articleId") Long articleId);

    List<CommentAdminDTO> listCommentsByParams(@Param("req") SearchCommentReq req, @Param("pageParam") PageParam pageParam);

    Long countCommentsByParams(@Param("req") SearchCommentReq req);

    /**
     * 批量统计子评论数量
     *
     * @param articleId 文章ID
     * @param topCommentIds 一级评论ID集合
     * @return Map<topCommentId, count>
     */
    List<Map<String, Object>> countSubCommentsByTopIds(@Param("articleId") Long articleId, @Param("topCommentIds") Collection<Long> topCommentIds);

    /**
     * 批量查询每个一级评论的第一条子评论
     *
     * @param articleId 文章ID
     * @param topCommentIds 一级评论ID集合
     * @return 每个一级评论的第一条子评论
     */
    List<CommentDO> listFirstSubCommentsByTopIds(@Param("articleId") Long articleId, @Param("topCommentIds") Collection<Long> topCommentIds);

}
