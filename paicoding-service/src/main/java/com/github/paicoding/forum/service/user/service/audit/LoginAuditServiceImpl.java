package com.github.paicoding.forum.service.user.service.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.paicoding.forum.api.model.enums.user.LoginAuditEventEnum;
import com.github.paicoding.forum.api.model.enums.user.LoginTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.UserSessionStateEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.user.SearchUserLoginAuditReq;
import com.github.paicoding.forum.api.model.vo.user.SearchUserShareRiskReq;
import com.github.paicoding.forum.api.model.vo.user.SearchUserSessionReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserActiveSessionDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserLoginAuditDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserActiveSessionDao;
import com.github.paicoding.forum.service.user.repository.dao.UserLoginAuditDao;
import com.github.paicoding.forum.service.user.repository.entity.UserActiveSessionDO;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录审计服务
 *
 * @author Codex
 * @date 2026/4/23
 */
@Service
public class LoginAuditServiceImpl implements LoginAuditService {
    private static final long MAX_PAGE_SIZE = 100L;
    private static final int DEFAULT_RISK_DAYS = 7;
    private static final int DEFAULT_MIN_KICKOUT_COUNT = 2;
    private static final int DEFAULT_MIN_DEVICE_COUNT = 2;
    private static final int DEFAULT_MIN_IP_COUNT = 2;

    @Autowired
    private UserLoginAuditDao userLoginAuditDao;

    @Autowired
    private UserAiDao userAiDao;

    @Autowired
    private UserActiveSessionDao userActiveSessionDao;

    @Autowired
    private UserShareRiskPolicy userShareRiskPolicy;

    @Override
    public void recordLoginSuccess(UserSessionHelper.SessionDeviceMeta sessionMeta, String sessionHash, String riskTag) {
        UserLoginAuditDO audit = buildAudit(sessionMeta, sessionHash);
        audit.setEventType(LoginAuditEventEnum.LOGIN_SUCCESS.getCode());
        audit.setRiskTag(riskTag);
        userLoginAuditDao.save(audit);
    }

    @Override
    public void recordLoginFail(String loginName, Integer loginType, String reason, UserSessionHelper.SessionDeviceMeta sessionMeta) {
        UserLoginAuditDO audit = buildAudit(sessionMeta, null);
        audit.setLoginName(loginName);
        audit.setLoginType(loginType);
        audit.setEventType(LoginAuditEventEnum.LOGIN_FAIL.getCode());
        audit.setReason(reason);
        userLoginAuditDao.save(audit);
    }

    @Override
    public void recordSessionOffline(String sessionHash, String reason, UserSessionHelper.SessionDeviceMeta sessionMeta, boolean kickout) {
        if (StringUtils.isBlank(sessionHash)) {
            return;
        }

        UserActiveSessionDO session = userActiveSessionDao.lambdaQuery()
                .eq(UserActiveSessionDO::getSessionHash, sessionHash)
                .orderByDesc(UserActiveSessionDO::getId)
                .last("limit 1")
                .one();
        if (session != null && session.getOfflineTime() == null) {
            session.setOfflineTime(new Date());
            session.setOfflineReason(reason);
            if (sessionMeta != null) {
                session.setIp(defaultIfBlank(sessionMeta.getIp(), session.getIp()));
                session.setRegion(defaultIfBlank(sessionMeta.getRegion(), session.getRegion()));
                session.setLatestSeenTime(toDate(sessionMeta.getLatestSeenTime(), session.getLatestSeenTime()));
            }
            userActiveSessionDao.updateById(session);
        }

        UserLoginAuditDO audit = buildAudit(sessionMeta, sessionHash);
        audit.setEventType(kickout ? LoginAuditEventEnum.SESSION_KICKOUT.getCode() : LoginAuditEventEnum.LOGOUT.getCode());
        audit.setReason(reason);
        if (audit.getUserId() == null && session != null) {
            audit.setUserId(session.getUserId());
            audit.setLoginName(session.getLoginName());
            audit.setLoginType(session.getLoginType());
            audit.setDeviceId(session.getDeviceId());
            audit.setDeviceName(session.getDeviceName());
            audit.setIp(session.getIp());
            audit.setRegion(session.getRegion());
        }
        userLoginAuditDao.save(audit);
    }

    @Override
    public void upsertActiveSession(UserSessionHelper.SessionDeviceMeta sessionMeta, String sessionHash) {
        if (sessionMeta == null || StringUtils.isBlank(sessionHash)) {
            return;
        }

        UserActiveSessionDO session = userActiveSessionDao.lambdaQuery()
                .eq(UserActiveSessionDO::getSessionHash, sessionHash)
                .orderByDesc(UserActiveSessionDO::getId)
                .last("limit 1")
                .one();
        if (session == null) {
            session = new UserActiveSessionDO();
        }

        session.setUserId(sessionMeta.getUserId());
        session.setLoginName(sessionMeta.getLoginName());
        session.setLoginType(sessionMeta.getLoginType());
        session.setSessionHash(sessionHash);
        session.setDeviceId(sessionMeta.getDeviceId());
        session.setDeviceName(sessionMeta.getDeviceName());
        session.setUaHash(sessionMeta.getUaHash());
        session.setUserAgent(sessionMeta.getUserAgent());
        session.setIp(sessionMeta.getIp());
        session.setRegion(sessionMeta.getRegion());
        session.setLatestSeenTime(toDate(sessionMeta.getLatestSeenTime(), new Date()));
        session.setExpireTime(toDate(sessionMeta.getExpireTime(), null));
        session.setOfflineTime(null);
        session.setOfflineReason(null);
        userActiveSessionDao.saveOrUpdate(session);
    }

    @Override
    public void touchActiveSession(String sessionHash, UserSessionHelper.SessionDeviceMeta sessionMeta) {
        if (StringUtils.isBlank(sessionHash) || sessionMeta == null) {
            return;
        }

        UserActiveSessionDO session = userActiveSessionDao.lambdaQuery()
                .eq(UserActiveSessionDO::getSessionHash, sessionHash)
                .orderByDesc(UserActiveSessionDO::getId)
                .last("limit 1")
                .one();
        if (session == null) {
            upsertActiveSession(sessionMeta, sessionHash);
            return;
        }

        session.setLatestSeenTime(toDate(sessionMeta.getLatestSeenTime(), new Date()));
        session.setExpireTime(toDate(sessionMeta.getExpireTime(), session.getExpireTime()));
        if (StringUtils.isNotBlank(sessionMeta.getIp())) {
            session.setIp(sessionMeta.getIp());
        }
        if (StringUtils.isNotBlank(sessionMeta.getRegion())) {
            session.setRegion(sessionMeta.getRegion());
        }
        if (StringUtils.isNotBlank(sessionMeta.getDeviceName())) {
            session.setDeviceName(sessionMeta.getDeviceName());
        }
        userActiveSessionDao.updateById(session);
    }

    @Override
    public void recordAccountForbid(UserDO user, String reason) {
        recordAccountEvent(user, LoginAuditEventEnum.ACCOUNT_FORBID.getCode(), reason);
    }

    @Override
    public void recordAccountUnforbid(UserDO user, String reason) {
        recordAccountEvent(user, LoginAuditEventEnum.ACCOUNT_UNFORBID.getCode(), reason);
    }

    @Override
    public PageVo<UserLoginAuditDTO> getLoginAuditPage(SearchUserLoginAuditReq req) {
        long pageNum = normalizePageNumber(req == null ? null : req.getPageNumber());
        long pageSize = normalizePageSize(req == null ? null : req.getPageSize());
        List<Long> starMatchedUserIds = resolveAuditUserIdsByStarNumber(req);
        String starNumber = req == null ? null : req.getStarNumber();

        LambdaQueryWrapper<UserLoginAuditDO> query = new LambdaQueryWrapper<>();
        query.eq(req != null && req.getUserId() != null, UserLoginAuditDO::getUserId, req.getUserId())
                .like(req != null && StringUtils.isNotBlank(req.getLoginName()), UserLoginAuditDO::getLoginName, req.getLoginName())
                .like(req != null && StringUtils.isNotBlank(req.getDeviceId()), UserLoginAuditDO::getDeviceId, req.getDeviceId())
                .like(req != null && StringUtils.isNotBlank(req.getIp()), UserLoginAuditDO::getIp, req.getIp())
                .eq(req != null && StringUtils.isNotBlank(req.getEventType()), UserLoginAuditDO::getEventType, req.getEventType())
                .orderByDesc(UserLoginAuditDO::getId);
        query.and(StringUtils.isNotBlank(starNumber), wrapper -> {
            wrapper.like(UserLoginAuditDO::getStarNumber, starNumber);
            if (!starMatchedUserIds.isEmpty()) {
                wrapper.or().in(UserLoginAuditDO::getUserId, starMatchedUserIds);
            }
        });

        Page<UserLoginAuditDO> page = userLoginAuditDao.page(new Page<>(pageNum, pageSize), query);
        List<UserLoginAuditDTO> list = page.getRecords().stream().map(this::toAuditDto).collect(Collectors.toList());
        fillStarNumber(list, UserLoginAuditDTO::getUserId, UserLoginAuditDTO::getStarNumber, UserLoginAuditDTO::setStarNumber);
        return PageVo.build(list, pageSize, pageNum, page.getTotal());
    }

    @Override
    public PageVo<UserActiveSessionDTO> getSessionPage(SearchUserSessionReq req) {
        long pageNum = normalizePageNumber(req == null ? null : req.getPageNumber());
        long pageSize = normalizePageSize(req == null ? null : req.getPageSize());
        Date now = new Date();

        LambdaQueryWrapper<UserActiveSessionDO> query = new LambdaQueryWrapper<>();
        query.eq(req != null && req.getUserId() != null, UserActiveSessionDO::getUserId, req.getUserId())
                .like(req != null && StringUtils.isNotBlank(req.getLoginName()), UserActiveSessionDO::getLoginName, req.getLoginName())
                .like(req != null && StringUtils.isNotBlank(req.getDeviceId()), UserActiveSessionDO::getDeviceId, req.getDeviceId())
                .like(req != null && StringUtils.isNotBlank(req.getIp()), UserActiveSessionDO::getIp, req.getIp())
                .isNull(req != null && Boolean.TRUE.equals(req.getActiveOnly()), UserActiveSessionDO::getOfflineTime)
                .gt(req != null && Boolean.TRUE.equals(req.getActiveOnly()), UserActiveSessionDO::getExpireTime, now)
                .orderByDesc(UserActiveSessionDO::getLatestSeenTime)
                .orderByDesc(UserActiveSessionDO::getId);

        Page<UserActiveSessionDO> page = userActiveSessionDao.page(new Page<>(pageNum, pageSize), query);
        List<UserActiveSessionDTO> list = page.getRecords().stream().map(this::toSessionDto).collect(Collectors.toList());
        return PageVo.build(list, pageSize, pageNum, page.getTotal());
    }

    @Override
    public PageVo<UserShareRiskDTO> getShareRiskPage(SearchUserShareRiskReq req) {
        SearchUserShareRiskReq searchReq = normalizeShareRiskReq(req);
        long pageNum = normalizePageNumber(searchReq.getPageNumber());
        long pageSize = normalizePageSize(searchReq.getPageSize());
        long offset = (pageNum - 1) * pageSize;

        Long total = userLoginAuditDao.countUserShareRisk(searchReq);
        if (total == null || total <= 0) {
            return PageVo.build(java.util.Collections.emptyList(), pageSize, pageNum, 0);
        }

        List<UserShareRiskDTO> list = userLoginAuditDao.listUserShareRisk(searchReq, offset, pageSize)
                .stream()
                .map(dto -> fillShareRiskDesc(dto, searchReq.getRecentDays()))
                .collect(Collectors.toList());
        fillStarNumber(list, UserShareRiskDTO::getUserId, UserShareRiskDTO::getStarNumber, UserShareRiskDTO::setStarNumber);
        return PageVo.build(list, pageSize, pageNum, total);
    }

    private UserLoginAuditDO buildAudit(UserSessionHelper.SessionDeviceMeta sessionMeta, String sessionHash) {
        UserLoginAuditDO audit = new UserLoginAuditDO();
        audit.setSessionHash(sessionHash);
        if (sessionMeta == null) {
            return audit;
        }
        audit.setUserId(sessionMeta.getUserId());
        audit.setLoginName(sessionMeta.getLoginName());
        audit.setStarNumber(sessionMeta.getStarNumber());
        audit.setLoginType(sessionMeta.getLoginType());
        audit.setDeviceId(sessionMeta.getDeviceId());
        audit.setDeviceName(sessionMeta.getDeviceName());
        audit.setUaHash(sessionMeta.getUaHash());
        audit.setUserAgent(sessionMeta.getUserAgent());
        audit.setIp(sessionMeta.getIp());
        audit.setRegion(sessionMeta.getRegion());
        return audit;
    }

    private void recordAccountEvent(UserDO user, String eventType, String reason) {
        if (user == null) {
            return;
        }
        UserLoginAuditDO audit = new UserLoginAuditDO();
        audit.setUserId(user.getId());
        audit.setLoginName(user.getUserName());
        audit.setStarNumber(resolveStarNumber(user.getId(), user.getUserName()));
        audit.setLoginType(user.getLoginType());
        audit.setEventType(eventType);
        audit.setReason(reason);
        userLoginAuditDao.save(audit);
    }

    private UserLoginAuditDTO toAuditDto(UserLoginAuditDO audit) {
        UserLoginAuditDTO dto = new UserLoginAuditDTO();
        dto.setId(audit.getId());
        dto.setUserId(audit.getUserId());
        dto.setLoginName(audit.getLoginName());
        dto.setStarNumber(audit.getStarNumber());
        dto.setLoginType(audit.getLoginType());
        dto.setLoginTypeDesc(descOfLoginType(audit.getLoginType()));
        dto.setEventType(audit.getEventType());
        dto.setEventTypeDesc(descOfEvent(audit.getEventType()));
        dto.setDeviceId(audit.getDeviceId());
        dto.setDeviceName(audit.getDeviceName());
        dto.setIp(audit.getIp());
        dto.setRegion(audit.getRegion());
        dto.setSessionHash(audit.getSessionHash());
        dto.setRiskTag(audit.getRiskTag());
        dto.setReason(audit.getReason());
        dto.setCreateTime(audit.getCreateTime());
        return dto;
    }

    private UserActiveSessionDTO toSessionDto(UserActiveSessionDO session) {
        UserActiveSessionDTO dto = new UserActiveSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setLoginName(session.getLoginName());
        dto.setLoginType(session.getLoginType());
        dto.setLoginTypeDesc(descOfLoginType(session.getLoginType()));
        dto.setSessionHash(session.getSessionHash());
        dto.setDeviceId(session.getDeviceId());
        dto.setDeviceName(session.getDeviceName());
        dto.setIp(session.getIp());
        dto.setRegion(session.getRegion());
        UserSessionStateEnum state = resolveState(session);
        dto.setSessionState(state.getCode());
        dto.setSessionStateDesc(state.getDesc());
        dto.setOfflineReason(session.getOfflineReason());
        dto.setLatestSeenTime(session.getLatestSeenTime());
        dto.setExpireTime(session.getExpireTime());
        dto.setOfflineTime(session.getOfflineTime());
        dto.setCreateTime(session.getCreateTime());
        return dto;
    }

    private UserShareRiskDTO fillShareRiskDesc(UserShareRiskDTO dto, int recentDays) {
        long kickoutCount = Optional.ofNullable(dto.getKickoutCount()).orElse(0L);
        long deviceCount = Optional.ofNullable(dto.getDeviceCount()).orElse(0L);
        long ipCount = Optional.ofNullable(dto.getIpCount()).orElse(0L);
        dto.setRiskLevel(userShareRiskPolicy.resolveRiskLevel(kickoutCount, deviceCount, ipCount));
        dto.setRiskReason(userShareRiskPolicy.buildRiskReason(recentDays, kickoutCount, deviceCount, ipCount));
        return dto;
    }

    private SearchUserShareRiskReq normalizeShareRiskReq(SearchUserShareRiskReq req) {
        SearchUserShareRiskReq target = Optional.ofNullable(req).orElseGet(SearchUserShareRiskReq::new);
        if (target.getRecentDays() == null || target.getRecentDays() <= 0) {
            target.setRecentDays(DEFAULT_RISK_DAYS);
        }
        if (target.getMinKickoutCount() == null || target.getMinKickoutCount() <= 0) {
            target.setMinKickoutCount(DEFAULT_MIN_KICKOUT_COUNT);
        }
        if (target.getMinDeviceCount() == null || target.getMinDeviceCount() <= 0) {
            target.setMinDeviceCount(DEFAULT_MIN_DEVICE_COUNT);
        }
        if (target.getMinIpCount() == null || target.getMinIpCount() <= 0) {
            target.setMinIpCount(DEFAULT_MIN_IP_COUNT);
        }
        return target;
    }

    private List<Long> resolveAuditUserIdsByStarNumber(SearchUserLoginAuditReq req) {
        if (req == null || StringUtils.isBlank(req.getStarNumber())) {
            return java.util.Collections.emptyList();
        }
        return userAiDao.listUserIdsByStarNumber(req.getStarNumber());
    }

    private UserSessionStateEnum resolveState(UserActiveSessionDO session) {
        if (session.getOfflineTime() != null) {
            return UserSessionStateEnum.OFFLINE;
        }
        if (session.getExpireTime() != null && session.getExpireTime().before(new Date())) {
            return UserSessionStateEnum.EXPIRED;
        }
        return UserSessionStateEnum.ACTIVE;
    }

    private String descOfLoginType(Integer loginType) {
        LoginTypeEnum typeEnum = LoginTypeEnum.fromType(loginType);
        return typeEnum == null ? "" : typeEnum.name();
    }

    private String descOfEvent(String eventType) {
        LoginAuditEventEnum eventEnum = LoginAuditEventEnum.fromCode(eventType);
        return eventEnum == null ? "" : eventEnum.getDesc();
    }

    private long normalizePageNumber(Long pageNumber) {
        return Optional.ofNullable(pageNumber)
                .filter(page -> page > 0)
                .orElse(PageParam.DEFAULT_PAGE_NUM);
    }

    private long normalizePageSize(Long pageSize) {
        return Math.min(Optional.ofNullable(pageSize)
                .filter(size -> size > 0)
                .orElse(PageParam.DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
    }

    private String defaultIfBlank(String val, String fallback) {
        return StringUtils.isNotBlank(val) ? val : fallback;
    }

    private Date toDate(Long val, Date fallback) {
        return val == null ? fallback : new Date(val);
    }

    private <T> void fillStarNumber(List<T> list,
                                    Function<T, Long> userIdGetter,
                                    Function<T, String> starNumberGetter,
                                    BiConsumer<T, String> starNumberSetter) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> userIds = list.stream()
                .map(userIdGetter)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> starNumberMap = userAiDao.getByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserAiDO::getUserId, UserAiDO::getStarNumber, (left, right) -> left));
        list.forEach(item -> {
            if (StringUtils.isNotBlank(starNumberGetter.apply(item))) {
                return;
            }
            String starNumber = starNumberMap.get(userIdGetter.apply(item));
            if (StringUtils.isNotBlank(starNumber)) {
                starNumberSetter.accept(item, starNumber);
            }
        });
    }

    private String resolveStarNumber(Long userId, String loginName) {
        if (userId != null) {
            UserAiDO userAi = userAiDao.getByUserId(userId);
            if (userAi != null && StringUtils.isNotBlank(userAi.getStarNumber())) {
                return userAi.getStarNumber();
            }
        }
        if (StringUtils.startsWith(loginName, "zsxq_")) {
            return StringUtils.substringAfter(loginName, "zsxq_");
        }
        return null;
    }
}
