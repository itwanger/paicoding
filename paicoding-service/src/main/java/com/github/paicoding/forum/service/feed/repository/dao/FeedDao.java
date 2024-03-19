package com.github.paicoding.forum.service.feed.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.feed.FeedViewEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.service.feed.repository.entity.FeedDO;
import com.github.paicoding.forum.service.feed.repository.entity.FeedTopicDO;
import com.github.paicoding.forum.service.feed.repository.mapper.FeedMapper;
import com.github.paicoding.forum.service.feed.repository.mapper.FeedTopicMapper;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * feed动态流
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Repository
public class FeedDao extends ServiceImpl<FeedMapper, FeedDO> {
    @Resource
    private FeedTopicMapper feedTopicMapper;

    public boolean saveFeedTopic(FeedTopicDO topic) {
        return feedTopicMapper.insert(topic) > 0;
    }

    /**
     * 更新计数
     *
     * @param topicIds
     */
    public void updateFeedTopicCount(List<Long> topicIds) {
        LambdaUpdateWrapper<FeedTopicDO> update = Wrappers.lambdaUpdate();
        update.setSql("cnt = cnt + 1")
                .in(FeedTopicDO::getId, topicIds)
                .eq(FeedTopicDO::getDeleted, YesOrNoEnum.NO.getCode());
        feedTopicMapper.update(null, update);
    }

    /**
     * 查询话题列表
     *
     * @param topics
     * @return
     */
    public List<FeedTopicDO> batchQueryFeedTopics(Collection<String> topics) {
        LambdaQueryWrapper<FeedTopicDO> contentQuery = Wrappers.lambdaQuery();
        contentQuery.in(FeedTopicDO::getTopic, topics)
                .eq(FeedTopicDO::getDeleted, YesOrNoEnum.NO.getCode());
        return feedTopicMapper.selectList(contentQuery);
    }

    /**
     * 计数更新
     *
     * @param feedId
     * @param commentCount
     * @param praiseCount
     */
    public boolean updateFeedCount(Long feedId, Integer commentCount, Integer praiseCount) {
        LambdaUpdateWrapper<FeedDO> update = Wrappers.lambdaUpdate();
        StringBuilder builder = new StringBuilder();
        if (commentCount != null) {
            builder.append("comment_count = comment_count ").append(commentCount > 0 ? "+" : "-").append(Math.abs(commentCount));
        }
        if (praiseCount != null) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("praise_count = praise_count ").append(praiseCount > 0 ? "+" : "-").append(Math.abs(praiseCount));
        }
        update.setSql(builder.toString())
                .eq(FeedDO::getId, feedId)
                .eq(FeedDO::getDeleted, YesOrNoEnum.NO.getCode());
        return baseMapper.update(null, update) > 0;
    }


    /**
     * 根据时间排序
     *
     * @param currentUser
     * @param pageParam
     * @return
     */
    public List<FeedDO> listFeedsByTime(Long currentUser, PageParam pageParam) {
        return listFeedsSortBy(currentUser, pageParam, FeedDO::getCreateTime);
    }

    /**
     * 根据时间排序
     *
     * @param currentUser
     * @param pageParam
     * @return
     */
    public List<FeedDO> listFeedsByPraiseCount(Long currentUser, PageParam pageParam) {
        return listFeedsSortBy(currentUser, pageParam, FeedDO::getPraiseCount);
    }

    private List<FeedDO> listFeedsSortBy(Long currentUser, PageParam pageParam, SFunction<FeedDO, ?> func) {
        LambdaQueryWrapper<FeedDO> contentQuery = Wrappers.lambdaQuery();
        List<Integer> views = Lists.newArrayList(FeedViewEnum.ALL.getValue());
        if (currentUser != null) {
            views.add(FeedViewEnum.LOGIN.getValue());
        }
        contentQuery.eq(FeedDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(FeedDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .in(FeedDO::getView, views)
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(func);
        return baseMapper.selectList(contentQuery);
    }

    public List<FeedDO> listFeedsByFollowed(Long currentUser, PageParam pageParam) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        return baseMapper.queryByFollow(currentUser, pageParam);
    }
}
