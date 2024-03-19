package com.github.paicoding.forum.service.feed.service;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedInfoDTO;

import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/18
 */
public interface FeedReadService {
    /**
     * 实时查询
     *
     * @param pageParam
     * @return
     */
    List<FeedInfoDTO> queryFeedsByTime(PageParam pageParam);

    /**
     * 根据点赞数查询
     *
     * @param pageParam
     * @return
     */
    List<FeedInfoDTO> queryFeedsByPraiseCount(PageParam pageParam);

    /**
     * 查询已关注的用户动态
     *
     * @param pageParam
     * @return
     */
    List<FeedInfoDTO> queryFeedsByFollowedUser(PageParam pageParam);
}
