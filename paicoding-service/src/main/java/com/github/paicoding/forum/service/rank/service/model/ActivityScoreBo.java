package com.github.paicoding.forum.service.rank.service.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class ActivityScoreBo {
    /**
     * 访问页面增加活跃度
     */
    private String path;

    /**
     * 目标文章
     */
    private Long articleId;

    /**
     * 评论增加活跃度
     */
    private Boolean rate;

    /**
     * 点赞增加活跃度
     */
    private Boolean praise;

    /**
     * 收藏增加活跃度
     */
    private Boolean collect;

    /**
     * 发布文章增加活跃度
     */
    private Boolean publishArticle;

    /**
     * 被关注的用户
     */
    private Long followedUserId;

    /**
     * 关注增加活跃度
     */
    private Boolean follow;
}
