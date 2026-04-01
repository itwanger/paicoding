package com.github.paicoding.forum.web.front.comment.vo;

import lombok.Data;

/**
 * 评论分页渲染结果
 */
@Data
public class CommentPageVo {
    /**
     * 评论片段 html
     */
    private String html;

    /**
     * 是否还有更多一级评论
     */
    private Boolean hasMore;

    /**
     * 下一页页码
     */
    private Long nextPageNum;
}
