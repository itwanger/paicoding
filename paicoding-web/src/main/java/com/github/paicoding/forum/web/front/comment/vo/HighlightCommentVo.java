package com.github.paicoding.forum.web.front.comment.vo;

import lombok.Data;

/**
 * @author YiHui
 * @date 2025/11/4
 */
@Data
public class HighlightCommentVo {
    /**
     * 划线评论id
     */
    private Long commentId;

    /**
     * 划线评论html
     */
    private String html;
}
