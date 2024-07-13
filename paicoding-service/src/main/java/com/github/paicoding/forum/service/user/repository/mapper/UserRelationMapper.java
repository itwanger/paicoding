package com.github.paicoding.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关系mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface UserRelationMapper extends BaseMapper<UserRelationDO> {

    /**
     * 我关注的用户
     * @param followUserId
     * @param pageParam
     * @return
     */
    List<FollowUserInfoDTO> queryUserFollowList(@Param("followUserId") Long followUserId, @Param("pageParam") PageParam pageParam);

    /**
     * 关注我的粉丝
     * @param userId
     * @param pageParam
     * @return
     */
    List<FollowUserInfoDTO> queryUserFansList(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);

    /**
     * userId对应的用户关注的用户的列表
     * @param page
     * @param userId
     * @return
     */
    IPage<FollowUserInfoDTO> queryUserFollowListPagination(IPage<FollowUserInfoDTO> page, @Param("userId") Long userId);

    IPage<FollowUserInfoDTO> queryUserFansListPagination(IPage<FollowUserInfoDTO> page, Long userId);
}
