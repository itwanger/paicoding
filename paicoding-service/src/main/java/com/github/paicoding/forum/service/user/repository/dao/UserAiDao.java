package com.github.paicoding.forum.service.user.repository.dao;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserMapper;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class UserAiDao extends ServiceImpl<UserAiMapper, UserAiDO> {

    @Autowired
    private UserAiMapper userAiMapper;

}
