package com.github.paicoding.forum.service.feed.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.beust.jcommander.internal.Lists;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.feed.FeedContentExtendExtraEnum;
import com.github.paicoding.forum.api.model.vo.feed.FeedSaveReq;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedContentExtra;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.LinkLoader;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.feed.conveter.FeedConverter;
import com.github.paicoding.forum.service.feed.repository.dao.FeedDao;
import com.github.paicoding.forum.service.feed.repository.entity.FeedDO;
import com.github.paicoding.forum.service.feed.repository.entity.FeedTopicDO;
import com.github.paicoding.forum.service.feed.service.FeedWriteService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Service
public class FeedWriteServiceImpl implements FeedWriteService {
    @Autowired
    private UserService userService;
    @Autowired
    private FeedDao feedDao;

    @Override
    @Transactional
    public boolean save(FeedSaveReq saveReq) {
        // 发表内容解析, 解析出url, @xxx, #xxx 话题圈
        Map<FeedContentExtendExtraEnum, List<FeedContentExtra>> extraMap = parseContent(saveReq);
        String extra = JsonUtil.toStr(extraMap);
        FeedDO feed = FeedConverter.toDo(saveReq, extra);

        // 1. 更新话题数
        List<FeedContentExtra> topicExtra = extraMap.get(FeedContentExtendExtraEnum.TOPIC);
        if (feed.getId() == null && !CollectionUtils.isEmpty(topicExtra)) {
            // 更新时，话题数不变
            List<Long> topicIds = topicExtra.stream().map(s -> Long.parseLong(s.getLink())).collect(Collectors.toList());
            feedDao.updateFeedTopicCount(topicIds);
        }

        // 2. 保存feed
        if (feed.getId() == null) {
            // 使用雪花算法生成动态主键
            feed.setId(IdUtil.genId());
        }
        feedDao.save(feed);

        // todo 3. 用户消息通知
        return true;
    }

    /**
     * 发表正文解析:
     * 正文中支持三种类型，都是通过 []() 方式传过来
     * - [@用户名](userId)
     * - [#话题](topicId)
     * - [链接](http://xxx)
     *
     * @param saveReq
     * @return
     */
    private Map<FeedContentExtendExtraEnum, List<FeedContentExtra>> parseContent(FeedSaveReq saveReq) {
        String content = saveReq.getContent();
        List<LinkLoader.Link> list = LinkLoader.loadLinks(content);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }

        Map<FeedContentExtendExtraEnum, List<FeedContentExtra>> result = Maps.newHashMap();
        for (LinkLoader.Link link : list) {
            FeedContentExtendExtraEnum extraEnum;
            if (link.getDesc().startsWith("@")) {
                extraEnum = FeedContentExtendExtraEnum.USER;
            } else if (link.getDesc().startsWith("#")) {
                extraEnum = FeedContentExtendExtraEnum.TOPIC;
            } else {
                extraEnum = FeedContentExtendExtraEnum.LINK;
            }

            result.computeIfAbsent(extraEnum, (k) -> Lists.newArrayList()).add(new FeedContentExtra().setType(extraEnum.name()).setTitle(link.getDesc()).setLink(StringUtils.isBlank(link.getUrl()) ? "" : link.getUrl().trim()));

            // 替换文本, 存储原始标识 + 一个截止符 π$，用于适配不同的前端展示跳转样式
            content = StringUtils.replace(content, link.getOrigin(), link.getDesc() + "π$");
            saveReq.setContent(content);
        }

        for (Map.Entry<FeedContentExtendExtraEnum, List<FeedContentExtra>> entry : result.entrySet()) {
            switch (entry.getKey()) {
                case USER:
                    this.fillExtraLink(entry.getValue(), this::fillExtraUserIdLink);
                    break;
                case TOPIC:
                    this.fillExtraLink(entry.getValue(), this::fillExtraTopicIdLink);
                    break;
                case LINK:
            }
        }
        return result;
    }

    private void fillExtraLink(List<FeedContentExtra> list, BiConsumer<Set<String>, List<FeedContentExtra>> consumer) {
        Set<String> names = list.stream().filter(extra -> !NumberUtil.isLong(extra.getLink())).map(extra -> extra.getTitle().substring(1)).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(names)) {
            consumer.accept(names, list);
        }
    }

    private void fillExtraUserIdLink(Set<String> names, List<FeedContentExtra> list) {
        List<SimpleUserInfoDTO> users = userService.batchQuerySimpleUserInfoByNickNames(names);
        Map<String, SimpleUserInfoDTO> map = users.stream().collect(Collectors.toMap(SimpleUserInfoDTO::getName, s -> s));
        list.forEach(feed -> {
            SimpleUserInfoDTO su = map.get(feed.getTitle().substring(1));
            feed.setLink(su == null ? "" : String.valueOf(su.getUserId()));
        });
    }

    private void fillExtraTopicIdLink(Set<String> topics, List<FeedContentExtra> list) {
        List<FeedTopicDO> topicList = feedDao.batchQueryFeedTopics(topics);
        Map<String, FeedTopicDO> map = topicList.stream().collect(Collectors.toMap(FeedTopicDO::getTopic, s -> s));
        list.forEach(feed -> {
            String strTopic = feed.getTitle().substring(1);
            FeedTopicDO topic = map.get(strTopic);
            if (topic == null) {
                FeedTopicDO t = new FeedTopicDO();
                t.setTopic(strTopic);
                t.setDeleted(YesOrNoEnum.NO.getCode());
                t.setCnt(0);
                t.setCreateTime(new Date());
                t.setUpdateTime(new Date());
                feedDao.saveFeedTopic(t);
                feed.setLink(String.valueOf(t.getId()));
            } else {
                feed.setLink(String.valueOf(topic.getId()));
            }
        });
    }

    @Override
    public boolean updateCommentCnt(Long feedId, int cnt) {
        return feedDao.updateFeedCount(feedId, cnt, null);
    }

    @Override
    public boolean updatePraiseCnt(Long feedId, int cnt) {
        return feedDao.updateFeedCount(feedId, null, cnt);
    }
}
