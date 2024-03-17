package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.dto.CurrentCommentDTO;

import java.util.List;

/**
 * APP适用版评论Service接口
 * - app与pc 评论的使用姿势不同，pc端，顶级评论展示，子评论全部放在顶级评论的下方
 * - app 所有评论平铺，区别在于非顶级评论，需要引用父评论的内容
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface AppCommentReadService {
    /**
     * 查询用户的评论列表
     *
     * @param userId
     * @param page
     * @return
     */
    List<CurrentCommentDTO> queryLatestCommentsByUser(Long userId, PageParam page);

    /**
     * 查询最新的评论列表
     *
     * @param articleId 文章
     * @param page      分页
     * @return
     */
    List<CurrentCommentDTO> queryLatestComments(Long articleId, PageParam page);
}
