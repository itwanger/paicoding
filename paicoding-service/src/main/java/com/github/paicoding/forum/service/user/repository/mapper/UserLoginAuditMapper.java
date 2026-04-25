package com.github.paicoding.forum.service.user.repository.mapper;

import com.github.paicoding.forum.api.model.vo.user.SearchUserShareRiskReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录审计日志
 *
 * @author Codex
 * @date 2026/4/23
 */
public interface UserLoginAuditMapper extends BaseMapper<UserLoginAuditDO> {
    List<UserShareRiskDTO> listUserShareRisk(@Param("req") SearchUserShareRiskReq req,
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);

    Long countUserShareRisk(@Param("req") SearchUserShareRiskReq req);
}
