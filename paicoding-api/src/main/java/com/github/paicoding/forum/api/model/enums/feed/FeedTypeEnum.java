package com.github.paicoding.forum.api.model.enums.feed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * feed动态类型
 *
 * @author YiHui
 * @date 2024/3/18
 */
@NoArgsConstructor
@AllArgsConstructor
public enum FeedTypeEnum {
    NORMAL_TYPE(0, "普通动态"),
    ARTICLE_FEED_TYPE(1, "转发文章的动态"),
    FEED_TYPE(2, "转发的动态"),
    COMMENT_FEED_TYPE(3, "转发的评论"),
    LINK_FEED_TYPE(4, "外部链接的动态"),
    ;
    @Getter
    private Integer type;
    @Getter
    private String desc;

    public static FeedTypeEnum typeOf(int type) {
        for (FeedTypeEnum feed : values()) {
            if (feed.type == type) {
                return feed;
            }
        }
        return null;
    }

}
