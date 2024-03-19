package com.github.paicoding.forum.service.feed.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.feed.FeedTypeEnum;
import com.github.paicoding.forum.api.model.enums.feed.FeedViewEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@TableName("feed")
@EqualsAndHashCode(callSuper = true)
public class FeedDO extends BaseDO {
    private static final long serialVersionUID = -2981445983545004105L;

    /**
     * 作者
     */
    private Long userId;

    /**
     * 话题id
     */
    private Long topicId;

    /**
     * 正文
     */
    private String content;

    /**
     * 扩展信息
     */
    private String extend;

    /**
     * 附图，英文逗号分割
     */
    private String img;

    /**
     * 类型
     *
     * @see FeedTypeEnum#getType()
     */
    private Integer type;

    /**
     * 分享的站内文章
     */
    private Long refId;

    /**
     * 分享的外部链接
     */
    private String refUrl;

    /**
     * 可见范围
     *
     * @see FeedViewEnum#getValue()
     */
    private Integer view;


    /**
     * 发布状态
     *
     * @see PushStatusEnum#getCode()
     */
    private Integer status;

    /**
     * 0 未删除 1 已删除
     *
     * @see YesOrNoEnum#getCode()
     */
    private Integer deleted;

    /**
     * 点赞数
     */
    private Integer praiseCount;

    /**
     * 评论数
     */
    private Integer commentCount;
}
