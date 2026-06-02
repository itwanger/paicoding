package com.github.paicoding.forum.api.model.vo.comment;

import com.github.paicoding.forum.api.model.vo.comment.dto.HighlightDto;
import lombok.Data;

/**
 * 划线 AI 实时回复请求
 */
@Data
public class HighlightAiStreamReq {
    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 引用的正文内容
     */
    private HighlightDto highlight;

    /**
     * AI 机器人：HATER_BOT / QA_BOT
     */
    private String bot;

    /**
     * 用户提交的评论内容
     */
    private String commentContent;

    /**
     * 用户附加问题，可为空
     */
    private String question;

    /**
     * 前端请求ID，用于忽略过期流
     */
    private String requestId;
}
