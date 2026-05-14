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
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserLoginAuditDao;
import com.github.paicoding.forum.service.user.repository.dao.UserShareRiskAccountDao;
import com.github.paicoding.forum.service.user.repository.entity.UserActiveSessionDO;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.entity.UserLoginAuditDO;
import com.github.paicoding.forum.service.user.repository.entity.UserShareRiskAccountDO;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.audit.UserShareRiskPolicy.HandleAction;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录审计与疑似共享账号视图。
 *
 * 写入路径（forbid/unforbid 时实时更新 user_share_risk_account）+ 读路径（getShareRiskPage 只读）；
 * 全量扫描下沉到 {@link UserShareRiskControlService} 的定时任务，列表请求不再做 ETL。
 *
 * @author Codex
 * @date 2026/4/23
 */
@Slf4j
@Service
public class LoginAuditServiceImpl implements LoginAuditService {
    private static final long MAX_PAGE_SIZE = 100L;
    private static final int CLEAR_BATCH_SIZE = 5000;
    private static final int CLEAR_MAX_BATCHES = 1000;

    @Autowired
    private UserLoginAuditDao userLoginAuditDao;

    @Autowired
    private UserAiDao userAiDao;

    @Autowired
    private UserActiveSessionDao userActiveSessionDao;

    @Autowired
    private UserShareRiskPolicy userShareRiskPolicy;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserShareRiskAccountDao userShareRiskAccountDao;

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
    public void recordAccountForbid(UserDO user, String reason, HandleAction action) {
        recordAccountEvent(user, LoginAuditEventEnum.ACCOUNT_FORBID.getCode(), reason);
        syncShareRiskAccountOnForbid(user, action);
    }

    @Override
    public void recordAccountUnforbid(UserDO user, String reason, HandleAction action) {
        recordAccountEvent(user, LoginAuditEventEnum.ACCOUNT_UNFORBID.getCode(), reason);
        syncShareRiskAccountOnUnforbid(user, action);
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
        fillUserNickname(list, UserLoginAuditDTO::getUserId, UserLoginAuditDTO::setUserNickname);
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

        LambdaQueryWrapper<UserShareRiskAccountDO> query = new LambdaQueryWrapper<>();
        query.like(StringUtils.isNotBlank(searchReq.getLoginName()), UserShareRiskAccountDO::getLoginName, searchReq.getLoginName())
                .like(StringUtils.isNotBlank(searchReq.getStarNumber()), UserShareRiskAccountDO::getStarNumber, searchReq.getStarNumber())
                .eq(searchReq.getRecentDays() != null, UserShareRiskAccountDO::getRecentDays, searchReq.getRecentDays())
                .ge(searchReq.getMinKickoutCount() != null, UserShareRiskAccountDO::getKickoutCount, searchReq.getMinKickoutCount())
                .and(searchReq.getMinDeviceCount() != null || searchReq.getMinIpCount() != null, wrapper -> wrapper
                        .ge(searchReq.getMinDeviceCount() != null, UserShareRiskAccountDO::getDeviceCount, searchReq.getMinDeviceCount())
                        .or()
                        .ge(searchReq.getMinIpCount() != null, UserShareRiskAccountDO::getIpCount, searchReq.getMinIpCount()))
                .orderByDesc(UserShareRiskAccountDO::getForbidden)
                .orderByDesc(UserShareRiskAccountDO::getKickoutCount)
                .orderByDesc(UserShareRiskAccountDO::getDeviceCount)
                .orderByDesc(UserShareRiskAccountDO::getIpCount)
                .orderByDesc(UserShareRiskAccountDO::getLastKickoutTime);

        Page<UserShareRiskAccountDO> page = userShareRiskAccountDao.page(new Page<>(pageNum, pageSize), query);
        List<UserShareRiskDTO> list = page.getRecords().stream().map(this::toShareRiskDto).collect(Collectors.toList());
        reconcileForbiddenStateForDisplay(list);
        fillStarNumber(list, UserShareRiskDTO::getUserId, UserShareRiskDTO::getStarNumber, UserShareRiskDTO::setStarNumber);
        fillUserNickname(list, UserShareRiskDTO::getUserId, UserShareRiskDTO::setUserNickname);
        return PageVo.build(list, pageSize, pageNum, page.getTotal());
    }

    @Override
    public int clearAllAuditData() {
        long beforeCount = userLoginAuditDao.count();
        if (beforeCount <= 0) {
            log.warn("clearAllAuditData triggered but table is empty");
            return 0;
        }

        long start = System.currentTimeMillis();
        int totalDeleted = 0;
        for (int i = 0; i < CLEAR_MAX_BATCHES; i++) {
            int deleted = userLoginAuditDao.deleteOldestBatch(CLEAR_BATCH_SIZE);
            if (deleted <= 0) {
                break;
            }
            totalDeleted += deleted;
        }
        log.warn("clearAllAuditData done, before={}, deleted={}, costMs={}",
                beforeCount, totalDeleted, System.currentTimeMillis() - start);
        return totalDeleted;
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

    private void syncShareRiskAccountOnForbid(UserDO user, HandleAction action) {
        if (user == null || user.getId() == null) {
            return;
        }
        UserShareRiskAccountDO existed = userShareRiskAccountDao.getByUserId(user.getId());
        UserShareRiskAccountDO target = existed != null ? existed : newRiskAccount(user);
        target.setUserId(user.getId());
        target.setLoginName(user.getUserName());
        target.setStarNumber(resolveStarNumber(user.getId(), user.getUserName()));
        target.setForbidden(true);
        target.setForbidUntil(user.getForbidUntil());
        target.setForbidReason(user.getForbidReason());
        target.setForbidOperatorId(user.getForbidOperatorId());
        if (action != null) {
            target.setLastHandleReason(action.text());
        }
        // 仅 forbid 不写 last_release_at；mapper 用 coalesce 保留旧值
        target.setLastReleaseAt(null);
        if (isZeroMetrics(target)) {
            target.setRiskLevel(UserShareRiskPolicy.LOW);
            target.setRiskReason(UserShareRiskPolicy.FORBIDDEN_ONLY_RISK_REASON);
        }
        userShareRiskAccountDao.upsert(target);
    }

    private void syncShareRiskAccountOnUnforbid(UserDO user, HandleAction action) {
        if (user == null || user.getId() == null) {
            return;
        }
        UserShareRiskAccountDO existed = userShareRiskAccountDao.getByUserId(user.getId());
        if (existed == null) {
            // 用户从未进过疑似名单，解禁动作不需要创建一条 placeholder
            return;
        }
        existed.setLoginName(user.getUserName());
        existed.setStarNumber(resolveStarNumber(user.getId(), user.getUserName()));
        existed.setForbidden(false);
        existed.setForbidUntil(null);
        existed.setForbidReason(null);
        existed.setForbidOperatorId(user.getForbidOperatorId());
        if (action != null) {
            existed.setLastHandleReason(action.text());
            if (action.isRelease()) {
                existed.setLastReleaseAt(new Date());
            }
        }
        if (isZeroMetrics(existed)) {
            existed.setRiskLevel(UserShareRiskPolicy.LOW);
            existed.setRiskReason(null);
        }
        userShareRiskAccountDao.upsert(existed);
    }

    private UserShareRiskAccountDO newRiskAccount(UserDO user) {
        UserShareRiskAccountDO account = new UserShareRiskAccountDO();
        account.setUserId(user.getId());
        account.setKickoutCount(0L);
        account.setLoginSuccessCount(0L);
        account.setDeviceCount(0L);
        account.setIpCount(0L);
        return account;
    }

    private void reconcileForbiddenStateForDisplay(List<UserShareRiskDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> userIds = list.stream()
                .map(UserShareRiskDTO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, UserDO> userMap = userDao.listUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (left, right) -> left));
        Date now = new Date();
        list.forEach(item -> {
            UserDO user = userMap.get(item.getUserId());
            if (user == null) {
                return;
            }
            boolean forbidden = user.getForbidUntil() != null && user.getForbidUntil().after(now);
            item.setForbidden(forbidden);
            item.setForbidUntil(forbidden ? user.getForbidUntil() : null);
            item.setForbidReason(forbidden ? user.getForbidReason() : null);
            if (!forbidden && UserShareRiskPolicy.FORBIDDEN_ONLY_RISK_REASON.equals(item.getRiskReason())) {
                item.setRiskReason(null);
            }
        });
    }

    private UserShareRiskDTO toShareRiskDto(UserShareRiskAccountDO account) {
        UserShareRiskDTO dto = new UserShareRiskDTO();
        dto.setUserId(account.getUserId());
        dto.setLoginName(account.getLoginName());
        dto.setStarNumber(account.getStarNumber());
        dto.setKickoutCount(account.getKickoutCount());
        dto.setLoginSuccessCount(account.getLoginSuccessCount());
        dto.setDeviceCount(account.getDeviceCount());
        dto.setIpCount(account.getIpCount());
        dto.setLastKickoutTime(account.getLastKickoutTime());
        dto.setLastActiveTime(account.getLastActiveTime());
        dto.setRiskLevel(account.getRiskLevel());
        dto.setRiskReason(account.getRiskReason());
        dto.setForbidden(account.getForbidden());
        dto.setForbidUntil(account.getForbidUntil());
        dto.setForbidReason(account.getForbidReason());
        dto.setRecentDays(account.getRecentDays());
        dto.setLastReleaseAt(account.getLastReleaseAt());
        dto.setLastHandleReason(account.getLastHandleReason());
        return dto;
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

    private SearchUserShareRiskReq normalizeShareRiskReq(SearchUserShareRiskReq req) {
        SearchUserShareRiskReq target = Optional.ofNullable(req).orElseGet(SearchUserShareRiskReq::new);
        if (target.getRecentDays() != null && target.getRecentDays() <= 0) {
            target.setRecentDays(null);
        }
        if (target.getMinKickoutCount() != null && target.getMinKickoutCount() <= 0) {
            target.setMinKickoutCount(null);
        }
        if (target.getMinDeviceCount() != null && target.getMinDeviceCount() <= 0) {
            target.setMinDeviceCount(null);
        }
        if (target.getMinIpCount() != null && target.getMinIpCount() <= 0) {
            target.setMinIpCount(null);
        }
        return target;
    }

    private List<Long> resolveAuditUserIdsByStarNumber(SearchUserLoginAuditReq req) {
        if (req == null || StringUtils.isBlank(req.getStarNumber())) {
            return Collections.emptyList();
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

    private boolean isZeroMetrics(UserShareRiskAccountDO account) {
        return zero(account.getKickoutCount())
                && zero(account.getDeviceCount())
                && zero(account.getIpCount());
    }

    private boolean zero(Long val) {
        return val == null || val == 0L;
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
                .filter(Objects::nonNull)
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

    private <T> void fillUserNickname(List<T> list,
                                      Function<T, Long> userIdGetter,
                                      BiConsumer<T, String> userNicknameSetter) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> userIds = list.stream()
                .map(userIdGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> nicknameMap = userDao.getByUserIds(userIds).stream()
                .filter(userInfo -> StringUtils.isNotBlank(userInfo.getUserName()))
                .collect(Collectors.toMap(UserInfoDO::getUserId, UserInfoDO::getUserName, (left, right) -> left));
        list.forEach(item -> {
            String nickname = nicknameMap.get(userIdGetter.apply(item));
            if (StringUtils.isNotBlank(nickname)) {
                userNicknameSetter.accept(item, nickname);
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
