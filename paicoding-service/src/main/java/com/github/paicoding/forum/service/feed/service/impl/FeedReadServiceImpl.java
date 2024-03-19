package com.github.paicoding.forum.service.feed.service.impl;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Sets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.feed.FeedTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedContentExtra;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedFootCountDTO;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedInfoDTO;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedRefDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.FeedUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.feed.repository.dao.FeedDao;
import com.github.paicoding.forum.service.feed.repository.entity.FeedDO;
import com.github.paicoding.forum.service.feed.service.FeedReadService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserRelationService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Service
public class FeedReadServiceImpl implements FeedReadService {
    @Autowired
    private FeedDao feedDao;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserFootService userFootService;
    @Autowired
    private ArticleReadService articleReadService;

    public List<FeedInfoDTO> queryFeedsByTime(PageParam pageParam) {
        Long user = ReqInfoContext.getReqInfo().getUserId();
        List<FeedDO> feeds = feedDao.listFeedsByTime(user, pageParam);
        return buildFeedInfo(user, feeds);
    }

    @Override
    public List<FeedInfoDTO> queryFeedsByPraiseCount(PageParam pageParam) {
        Long user = ReqInfoContext.getReqInfo().getUserId();
        List<FeedDO> feeds = feedDao.listFeedsByPraiseCount(user, pageParam);
        return buildFeedInfo(user, feeds);
    }

    @Override
    public List<FeedInfoDTO> queryFeedsByFollowedUser(PageParam pageParam) {
        Long user = ReqInfoContext.getReqInfo().getUserId();
        List<FeedDO> feeds = feedDao.listFeedsByFollowed(user, pageParam);
        return buildFeedInfo(user, feeds);
    }

    private List<FeedInfoDTO> buildFeedInfo(Long user, List<FeedDO> feeds) {
        if (CollectionUtils.isEmpty(feeds)) {
            return Collections.emptyList();
        }

        // 查询用户信息
        Set<Long> authUsers = Sets.newHashSet();
        // 查询统计信息
        List<Long> feedIds = Lists.newArrayList(feeds.size());
        feeds.forEach(feed -> {
            authUsers.add(feed.getUserId());
            feedIds.add(feed.getId());
        });

        // 用户信息
        Map<Long, FeedUserInfoDTO> users = initFeedUserInfo(user, authUsers);

        // 当前用户是否有点赞
        List<UserFootDO> footList = userFootService.batchQueryUserFoot(feedIds, DocumentTypeEnum.FEED.getCode(), user);
        Map<Long, UserFootDO> footMap = footList.stream().collect(Collectors.toMap(UserFootDO::getDocumentId, s -> s));

        return feeds.stream().map(feed -> {
            FeedInfoDTO info = new FeedInfoDTO();
            info.setId(feed.getId());
            info.setContent(feed.getContent());
            TypeReference<HashMap<String, List<FeedContentExtra>>> type = new TypeReference<HashMap<String, List<FeedContentExtra>>>() {
            };
            info.setExtra(JsonUtil.toObj(feed.getExtend(), type));
            if (!StringUtils.isBlank(feed.getImg())) {
                info.setImgs(Splitter.on(",").splitToList(feed.getImg()));
            }
            info.setView(feed.getView());
            info.setType(feed.getType());
            info.setPraised(false);
            info.setUser(users.get(feed.getUserId()));
            info.setPraised(footMap.containsKey(feed.getId()) && footMap.get(feed.getId()).getPraiseStat() == YesOrNoEnum.YES.getCode());
            info.setCreateTime(feed.getCreateTime().getTime());
            info.setUpdateTime(feed.getUpdateTime().getTime());

            // 转发的内容
            info.setRefInfo(initRefInfo(feed));
            // 计数信息更新
            info.setCount(new FeedFootCountDTO().setPraiseCount(feed.getPraiseCount()).setCommentCount(feed.getCommentCount()));
            return info;
        }).collect(Collectors.toList());
    }


    private Map<Long, FeedUserInfoDTO> initFeedUserInfo(Long loginUser, Set<Long> authUsers) {
        List<BaseUserInfoDTO> users = userService.batchQueryBasicUserInfo(authUsers);
        List<FeedUserInfoDTO> list = users.stream().map(s -> {
            FeedUserInfoDTO info = new FeedUserInfoDTO();
            info.setUserId(s.getUserId());
            info.setUserName(s.getUserName());
            info.setAvatar(s.getPhoto());
            info.setProfile(s.getProfile());
            info.setRegion(s.getRegion());
            info.setFollowed(false);
            return info;
        }).collect(Collectors.toList());

        if (loginUser != null) {
            userRelationService.updateUserFollowRelationId(list, loginUser);
        }

        return list.stream().collect(Collectors.toMap(FollowUserInfoDTO::getUserId, s -> s));
    }

    private FeedRefDTO initRefInfo(FeedDO feed) {
        FeedTypeEnum type = FeedTypeEnum.typeOf(feed.getType());
        if (type == null) {
            return null;
        }

        switch (type) {
            case FEED_TYPE:
                // 转发的动态
                return initFeedRef(feed.getRefId());
            case ARTICLE_FEED_TYPE:
                // 转发的文章
                return initArticleRef(feed.getRefId());
            case LINK_FEED_TYPE:
                // 转发的普通url
                return initLinRef(feed.getRefUrl());
            case NORMAL_TYPE:
                // 普通动态;
            case COMMENT_FEED_TYPE:
                // 转发的评论
            default:
                return null;
        }
    }

    private FeedRefDTO initFeedRef(Long id) {
        FeedDO feed = feedDao.getById(id);
        SimpleUserInfoDTO user = userService.querySimpleUserInfo(feed.getUserId());
        FeedRefDTO dto = new FeedRefDTO();
        dto.setId(feed.getId())
                .setTitle(user.getName())
                .setContent(feed.getContent())
                .setType(FeedTypeEnum.FEED_TYPE.getType())
                .setImgs(StringUtils.isNoneBlank(feed.getImg()) ? Splitter.on(",").splitToList(feed.getImg()) : Collections.emptyList());
        return dto;
    }

    private FeedRefDTO initArticleRef(Long id) {
        ArticleDO article = articleReadService.queryBasicArticle(id);
        return new FeedRefDTO()
                .setId(article.getId())
                .setImgs(StringUtils.isNoneBlank(article.getPicture()) ? Arrays.asList(article.getPicture()) : Collections.emptyList())
                .setTitle(article.getTitle())
                .setContent(article.getSummary())
                .setType(FeedTypeEnum.ARTICLE_FEED_TYPE.getType());
    }

    private FeedRefDTO initLinRef(String link) {
        return new FeedRefDTO().setUrl(link).setType(FeedTypeEnum.LINK_FEED_TYPE.getType());
    }

}
