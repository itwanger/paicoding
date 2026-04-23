package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.user.SearchUserLoginAuditReq;
import com.github.paicoding.forum.api.model.vo.user.SearchUserSessionReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserActiveSessionDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserLoginAuditDTO;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;

/**
 * 登录审计与会话巡检
 *
 * @author Codex
 * @date 2026/4/23
 */
public interface LoginAuditService {
    void recordLoginSuccess(UserSessionHelper.SessionDeviceMeta sessionMeta, String sessionHash, String riskTag);

    void recordLoginFail(String loginName, Integer loginType, String reason, UserSessionHelper.SessionDeviceMeta sessionMeta);

    void recordSessionOffline(String sessionHash, String reason, UserSessionHelper.SessionDeviceMeta sessionMeta, boolean kickout);

    void upsertActiveSession(UserSessionHelper.SessionDeviceMeta sessionMeta, String sessionHash);

    void touchActiveSession(String sessionHash, UserSessionHelper.SessionDeviceMeta sessionMeta);

    PageVo<UserLoginAuditDTO> getLoginAuditPage(SearchUserLoginAuditReq req);

    PageVo<UserActiveSessionDTO> getSessionPage(SearchUserSessionReq req);
}
