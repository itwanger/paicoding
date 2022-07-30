package com.github.liuyueyi.forum.service.user.dto;

import lombok.Data;

/**
 * 文章足迹计数
 *
 * @author louzai
 * @date 2022-07-18
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
}
