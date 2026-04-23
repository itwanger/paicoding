package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserLoginAuditMapper;
import org.springframework.stereotype.Repository;

/**
 * 登录审计日志
 *
 * @author Codex
 * @date 2026/4/23
 */
@Repository
public class UserLoginAuditDao extends ServiceImpl<UserLoginAuditMapper, UserLoginAuditDO> {
}
