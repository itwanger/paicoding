package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;

/**
 * 文章足迹计数
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ArticleFootCountDTO {

    /**
     * 文章点赞数
     */
    private Integer  praiseCount;

    /**
     * 文章被阅读数
     */
    private Integer  readCount;

    /**
     * 文章被收藏数
     */
    private Integer  collectionCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    public ArticleFootCountDTO() {
        praiseCount = 0;
        readCount = 0;
        collectionCount = 0;
        commentCount = 0;
    }
}
