package com.github.liuyueyi.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关系mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserRelationMapper extends BaseMapper<UserRelationDO> {

    /**
     * 分页查询关注用户
     * @param userId
     * @param pageParam
     * @return
     */
    List<UserFollowDTO> queryUserFollow(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);
}
