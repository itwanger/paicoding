package com.github.paicoding.forum.api.model.vo.feed.dto;

import com.github.paicoding.forum.api.model.enums.feed.FeedContentExtendExtraEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@Accessors(chain = true)
public class FeedContentExtra {
    /**
     * 类型
     *
     * @see FeedContentExtendExtraEnum#name()
     */
    private String type;

    private String title;

    /**
     * type == user, 存 userId
     * type == topic, 存 feedTopicId
     * type == link, 存 http url
     */
    private String link;
}
