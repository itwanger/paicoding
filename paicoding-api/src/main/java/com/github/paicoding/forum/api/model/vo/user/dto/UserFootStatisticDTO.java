package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class UserFootStatisticDTO {

    /**
     * 文章点赞数
     */
    private Long praiseCount;

    /**
     * 文章被阅读数
     */
    private Long readCount;

    /**
     * 文章被收藏数
     */
    private Long collectionCount;

    /**
     * 文章被评论数
     */
    private Long commentCount;

    public UserFootStatisticDTO() {
        praiseCount = 0L;
        readCount = 0L;
        collectionCount = 0L;
        commentCount = 0L;
    }
}
