package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.FollowStateEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 用户相关DB操作
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class UserRelationDao extends ServiceImpl<UserRelationMapper, UserRelationDO> {

    /**
     * 查询用户的关注列表
     *
     * @param followUserId
     * @param pageParam
     * @return
     */
    public List<FollowUserInfoDTO> listUserFollows(Long followUserId, PageParam pageParam) {
        return baseMapper.queryUserFollowList(followUserId, pageParam);
    }

    /**
     * 查询用户的粉丝列表，即关注userId的用户
     *
     * @param userId
     * @param pageParam
     * @return
     */
    public List<FollowUserInfoDTO> listUserFans(Long userId, PageParam pageParam) {
        return baseMapper.queryUserFansList(userId, pageParam);
    }

    /**
     * 查询followUserId与给定的用户列表的关联关旭
     *
     * @param followUserId 粉丝用户id
     * @param targetUserId 关注者用户id列表
     * @return
     */
    public List<UserRelationDO> listUserRelations(Long followUserId, Collection<Long> targetUserId) {
        return lambdaQuery().eq(UserRelationDO::getFollowUserId, followUserId)
                .in(UserRelationDO::getUserId, targetUserId).list();
    }

    public Long queryUserFollowCount(Long userId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getFollowUserId, userId)
                .eq(UserRelationDO::getFollowState, FollowStateEnum.FOLLOW.getCode());
        return baseMapper.selectCount(queryWrapper);
    }

    public Long queryUserFansCount(Long userId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getUserId, userId)
                .eq(UserRelationDO::getFollowState, FollowStateEnum.FOLLOW.getCode());
        return baseMapper.selectCount(queryWrapper);
    }

    /**
     * 获取关注信息
     *
     * @param userId       登录用户
     * @param followUserId 关注的用户
     * @return
     */
    public UserRelationDO getUserRelationByUserId(Long userId, Long followUserId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getUserId, userId)
                .eq(UserRelationDO::getFollowUserId, followUserId)
                .eq(UserRelationDO::getFollowState, FollowStateEnum.FOLLOW.getCode());
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取关注记录
     *
     * @param userId       登录用户
     * @param followUserId 关注的用户
     * @return
     */
    public UserRelationDO getUserRelationRecord(Long userId, Long followUserId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getUserId, userId)
                .eq(UserRelationDO::getFollowUserId, followUserId);
        return baseMapper.selectOne(queryWrapper);
    }

    public IPage<FollowUserInfoDTO> listUserFollowsPagination(int currentPage, int pageSize, Long userId) {
        Page<FollowUserInfoDTO> page = new Page<>(currentPage, pageSize);

        return baseMapper.queryUserFollowListPagination(page, userId);
    }

    public IPage<FollowUserInfoDTO> listUserFansPagination(int currentPage, int pageSize, Long userId) {
        Page<FollowUserInfoDTO> page = new Page<>(currentPage, pageSize);

        return baseMapper.queryUserFansListPagination(page, userId);
    }
}