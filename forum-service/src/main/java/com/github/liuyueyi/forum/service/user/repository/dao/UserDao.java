package com.github.liuyueyi.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class UserDao extends ServiceImpl<UserInfoMapper, UserInfoDO> {
    @Resource
    private UserMapper userMapper;

    public UserDO getByThirdAccountId(String accountId) {
        return userMapper.getByThirdAccountId(accountId);
    }

    public void saveUser(UserDO user) {
        userMapper.insert(user);
    }

    public UserInfoDO getByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return baseMapper.selectOne(query);
    }

    public Integer getUserCount() {
        return lambdaQuery()
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count().intValue();
    }
}
