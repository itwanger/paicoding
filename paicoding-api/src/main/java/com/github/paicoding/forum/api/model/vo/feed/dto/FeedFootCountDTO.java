package com.github.paicoding.forum.api.model.vo.feed.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 信息流的统计信息
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@Accessors(chain = true)
public class FeedFootCountDTO implements Serializable {
    private static final long serialVersionUID = 3589508159429676481L;

    /**
     * 文章点赞数
     */
    @ApiModelProperty("点赞数")
    private Integer praiseCount;

    /**
     * 评论数
     */
    @ApiModelProperty("评论数")
    private Integer commentCount;

    public FeedFootCountDTO() {
        praiseCount = 0;
        commentCount = 0;
    }
}
