package com.github.liuyueyi.forum.service.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.liueyueyi.forum.api.model.enums.FollowStateEnum;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
@Service
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserRelationMapper userRelationMapper;

    @Override
    public Long queryUserFollowCount(Long userId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getFollowUserId, userId)
                .eq(UserRelationDO::getFollowState, FollowStateEnum.FOLLOW.getCode());
        return userRelationMapper.selectCount(queryWrapper);
    }

    @Override
    public Long queryUserFansCount(Long userId) {
        QueryWrapper<UserRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserRelationDO::getUserId, userId)
                .eq(UserRelationDO::getFollowState, FollowStateEnum.FOLLOW.getCode());
        return userRelationMapper.selectCount(queryWrapper);
    }
}