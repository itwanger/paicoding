package com.github.paicoding.forum.service.feed.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.service.feed.repository.entity.FeedDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/18
 */
public interface FeedMapper extends BaseMapper<FeedDO> {

    /**
     * 通够联表查询关注的用户动态
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<FeedDO> queryByFollow(Long userId, @Param("pageParam") PageParam pageParam);

}
