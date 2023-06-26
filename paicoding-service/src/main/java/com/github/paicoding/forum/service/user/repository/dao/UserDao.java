package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.UserAIStatEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserMapper;
import com.github.paicoding.forum.service.user.service.help.UserRandomGenHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * UserDao
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class UserDao extends ServiceImpl<UserInfoMapper, UserInfoDO> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAiMapper userAiMapper;

    /**
     * 密码加盐，更推荐的做法是每个用户都使用独立的盐，提高安全性
     */
    @Value("${security.salt}")
    private String salt;

    @Value("${security.salt-index}")
    private Integer saltIndex;

    /**
     * 注册用户
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAiDO registerUser(String userName, String password) {
        UserDO userDO = new UserDO();
        userDO.setUserName(userName);
        if (password.length() > saltIndex) {
            password = password.substring(0, saltIndex) + salt + password.substring(saltIndex);
        } else {
            password = password + salt;
        }
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        userDO.setPassword(password);
        userDO.setThirdAccountId("starNumber"+userName);
        // 暂定为 10
        userDO.setLoginType(10);

        userMapper.insert(userDO);

        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(userDO.getId());
        userInfo.setUserName(UserRandomGenHelper.genNickName());
        userInfo.setPhoto(UserRandomGenHelper.genAvatar());
        userInfoMapper.insert(userInfo);

        UserAiDO userAiDO = new UserAiDO();
        userAiDO.setUserId(userDO.getId());
        userAiDO.setStarNumber(userName);
        userAiDO.setStarType(1);
        userAiDO.setInviterUserId(-1);
        userAiDO.setInviteCode("-1");
        userAiDO.setState(UserAIStatEnum.TRYING.getCode());
        userAiMapper.insert(userAiDO);

        return userAiDO;

    }

    /**
     * 三方账号登录方式
     *
     * @param accountId
     * @return
     */
    public UserDO getByThirdAccountId(String accountId) {
        return userMapper.getByThirdAccountId(accountId);
    }

    /**
     * 根据用户名来查询
     *
     * @param userName
     * @return
     */
    public List<UserInfoDO> getByUserNameLike(String userName) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.select(UserInfoDO::getUserId, UserInfoDO::getUserName, UserInfoDO::getPhoto, UserInfoDO::getProfile)
                .and(!StringUtils.isEmpty(userName),
                        v -> v.like(UserInfoDO::getUserName, userName)
                )
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return baseMapper.selectList(query);
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

    public List<UserInfoDO> getByUserIds(Collection<Long> userIds) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.in(UserInfoDO::getUserId, userIds)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return baseMapper.selectList(query);
    }

    public Long getUserCount() {
        return lambdaQuery()
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count();
    }

    public void updateUserInfo(UserInfoDO user) {
        UserInfoDO record = getByUserId(user.getUserId());
        if (record.equals(user)) {
            return;
        }
        if (StringUtils.isEmpty(user.getPhoto())) {
            user.setPhoto(null);
        }
        if (StringUtils.isEmpty(user.getUserName())) {
            user.setUserName(null);
        }
        user.setId(record.getId());
        updateById(user);
    }

    public UserDO getByUserName(String userName) {
        LambdaQueryWrapper<UserDO> queryUser = Wrappers.lambdaQuery();
        queryUser.eq(UserDO::getUserName, userName)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userMapper.selectOne(queryUser);
    }
}
