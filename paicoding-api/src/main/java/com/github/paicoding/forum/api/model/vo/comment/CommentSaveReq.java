package com.github.paicoding.forum.api.model.vo.comment;

import com.github.paicoding.forum.api.model.vo.comment.dto.HighlightDto;
import lombok.Data;

/**
 * 评论列表入参
 *
 * @author louzai
 * @date 2022-07-24
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

    /**
     * 引用的正文内容
     */
    private HighlightDto highlight;

    /**
     * 是否跳过 AI 机器人触发；用于调用方自行接管实时回复流程。
     */
    private Boolean skipAiTrigger;
}
