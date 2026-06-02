package com.github.paicoding.forum.api.model.vo.comment;

import lombok.Data;

/**
 * 评论区 AI 实时回复请求
 */
@Data
public class CommentAiStreamReq {
    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * AI 机器人：HATER_BOT / QA_BOT
     */
    private String bot;

    /**
     * 用户提交的评论内容
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
     * 用户附加问题，可为空
     */
    private String question;

    /**
     * 前端请求ID，用于忽略过期流
     */
    private String requestId;
}
