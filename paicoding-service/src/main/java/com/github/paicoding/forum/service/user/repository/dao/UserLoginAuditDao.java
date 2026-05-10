package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.vo.user.SearchUserShareRiskReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserLoginAuditMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 登录审计日志
 *
 * @author Codex
 * @date 2026/4/23
 */
@Repository
public class UserLoginAuditDao extends ServiceImpl<UserLoginAuditMapper, UserLoginAuditDO> {
    public List<UserShareRiskDTO> listUserShareRisk(SearchUserShareRiskReq req, long offset, long limit) {
        return baseMapper.listUserShareRisk(req, offset, limit);
    }

    public Long countUserShareRisk(SearchUserShareRiskReq req) {
        return baseMapper.countUserShareRisk(req);
    }

    public List<UserShareRiskDTO> listHighRiskUsers(int recentDays) {
        return baseMapper.listHighRiskUsers(recentDays);
    }

    public UserShareRiskDTO getUserShareRisk(Long userId, int recentDays) {
        return baseMapper.getUserShareRisk(userId, recentDays);
    }
}
