package com.github.paicoding.forum.web.javabetter.spring1;

import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceV2 {

    @Resource
    private UserDao userDao;

    @Autowired
    @Qualifier("userRepository21")
    private UserRepository2 userRepository21;

    @Resource(name = "userRepository22")
    private UserRepository2 userRepository22;

    // 查询用户信息并缓存结果
    @Cacheable(value = "userCache", key = "#userId")
    public UserInfoDO getUserById(Long userId) {
        // 模拟数据库访问
        return userDao.getByUserId(userId);
    }

    // 更新用户信息并更新缓存
    @CachePut(value = "userCache", key = "#user.id")
    public boolean updateUser(UserInfoDO user) {
        return userDao.updateById(user);
    }

    // 删除缓存
    @CacheEvict(value = "userCache", key = "#userId")
    public void deleteUser(Long userId) {
        userDao.removeById(userId);
    }
}
