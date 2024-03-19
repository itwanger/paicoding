package com.github.paicoding.forum.service.feed.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@TableName("feed_topic")
@EqualsAndHashCode(callSuper = true)
public class FeedTopicDO extends BaseDO {

    /**
     * 话题
     */
    private String topic;

    /**
     * 计数
     */
    private Integer cnt;

    /**
     * 0 未删除 1 已删除
     *
     * @see YesOrNoEnum#getCode()
     */
    private Integer deleted;
}
