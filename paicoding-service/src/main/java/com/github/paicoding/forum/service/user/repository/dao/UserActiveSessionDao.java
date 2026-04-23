package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.user.repository.entity.UserActiveSessionDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserActiveSessionMapper;
import org.springframework.stereotype.Repository;

/**
 * 活跃登录会话
 *
 * @author Codex
 * @date 2026/4/23
 */
@Repository
public class UserActiveSessionDao extends ServiceImpl<UserActiveSessionMapper, UserActiveSessionDO> {
}
