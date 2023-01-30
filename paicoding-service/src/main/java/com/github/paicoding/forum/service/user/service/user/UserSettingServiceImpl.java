package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.service.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户权限管理后台接口
 *
 * @author louzai
 * @date 2022-09-20
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {

    @Autowired
    private UserDao userDao;

    @Override
    public Integer getUserCount() {
        return userDao.getUserCount();
    }
}
