package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.user.repository.entity.UserAiHistoryDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiHistoryMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;

@Repository
public class UserAiHistoryDao extends ServiceImpl<UserAiHistoryMapper, UserAiHistoryDO> {

    @Resource
    private UserAiHistoryMapper userAiHistoryMapper;
}

