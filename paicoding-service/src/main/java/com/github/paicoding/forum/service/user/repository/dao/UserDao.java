package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class UserDao extends ServiceImpl<UserInfoMapper, UserInfoDO> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

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
    public void registerUser(String userName, String password) {

        UserDO userDO = new UserDO();
        userDO.setUserName(userName);
        if (password.length() > saltIndex) {
            password = password.substring(0, saltIndex) + salt + password.substring(saltIndex);
        } else {
            password = password + salt;
        }
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        userDO.setPassword(password);
        userDO.setThirdAccountId("default");
        userDO.setStarNumber("-1");

        userMapper.insert(userDO);

        UserInfoDO userInfoDO = new UserInfoDO();
        userInfoDO.setUserId(Long.valueOf(userDO.getId()));
        userInfoDO.setUserName(userName);
        userInfoDO.setPhoto("https://cdn.tobebetterjavaer.com/paicoding/avatar/0057.png");
        userInfoMapper.insert(userInfoDO);

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
     * 用户名登录
     *
     * @param userName
     * @return
     */
    public UserDO getByUserName(String userName) {
        LambdaQueryWrapper<UserDO> query = Wrappers.lambdaQuery();

        // 支持userName or starNumber查询
        query.and(wrapper -> wrapper.eq(UserDO::getUserName, userName).or().eq(UserDO::getStarNumber, userName))
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode());
        /*query.eq(UserDO::getUserName, userName)
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode());*/
        return userMapper.selectOne(query);
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

    /**
     * user和星球编号做绑定
     *
     * @author: ygl
     * @date: 2023/6/22 10:56
     * @return
     */
    public void register(String username, Integer starNumber) {

        UserDO userDO = this.getByUserName(username);
        userDO.setStarNumber(String.valueOf(starNumber));
        userMapper.updateById(userDO);

    }
}
