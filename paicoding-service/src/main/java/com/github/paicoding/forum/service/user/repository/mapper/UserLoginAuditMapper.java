package com.github.paicoding.forum.service.user.repository.mapper;

import com.github.paicoding.forum.api.model.vo.user.SearchUserShareRiskReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

    List<UserShareRiskDTO> listHighRiskUsers(@Param("recentDays") int recentDays);

    UserShareRiskDTO getUserShareRisk(@Param("userId") Long userId, @Param("recentDays") int recentDays);

    UserShareRiskDTO getUserShareRiskAfter(@Param("userId") Long userId, @Param("startTime") Date startTime);

    /**
     * 按 id 升序删除最旧的一批，避免一次性全表删除造成长事务/锁等待。
     */
    @Delete("delete from user_login_audit order by id asc limit #{limit}")
    int deleteOldestBatch(@Param("limit") int limit);
}
