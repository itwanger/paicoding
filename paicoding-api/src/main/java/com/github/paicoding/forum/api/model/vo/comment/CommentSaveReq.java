package com.github.paicoding.forum.api.model.vo.comment;

import lombok.Data;

/**
 * 评论列表入参
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class CommentSaveReq {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 父评论ID
     */
    private Long parentCommentId;

    /**
     * 顶级评论ID
     */
    private Long topCommentId;
}
