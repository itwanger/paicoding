package com.github.paicoding.forum.service.user.service.audit;

import com.github.paicoding.forum.api.model.vo.user.SearchUserShareRiskReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserLoginAuditDao;
import com.github.paicoding.forum.service.user.repository.dao.UserShareRiskAccountDao;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserShareRiskAccountDO;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.audit.UserShareRiskPolicy.HandleAction;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 疑似共享账号自动管控
 * <p>
 * 写入路径：定时任务 {@link #scanAndUpsertRiskAccountsBySchedule()} 每日把 audit 聚合后的疑似账号
 * upsert 到 {@code user_share_risk_account}；admin 列表只读这张表。
 *
 * @author Codex
 * @date 2026/5/10
 */
@Slf4j
@Service
public class UserShareRiskControlService {
    private static final int AUTO_CHECK_RECENT_DAYS = 7;
    private static final int SCAN_MIN_KICKOUT = 2;
    private static final int SCAN_MIN_DEVICE = 2;
    private static final int SCAN_MIN_IP = 2;
    private static final long SCAN_BATCH_SIZE = 500L;
    private static final Long SYSTEM_OPERATOR_ID = 0L;
    private static final String ZSXQ_RELEASE_LOCK_KEY_PREFIX = "share-risk:zsxq-release:";
    private static final long ZSXQ_RELEASE_LOCK_TTL_SECONDS = 60L;
    public static final String HIGH_RISK_LOGIN_TIP = "账号近期存在频繁切换IP/设备的风险记录，请尽量固定常用设备和网络登录";

    private final UserLoginAuditDao userLoginAuditDao;
    private final UserDao userDao;
    private final UserSessionHelper userSessionHelper;
    private final LoginAuditService loginAuditService;
    private final UserShareRiskPolicy userShareRiskPolicy;
    private final UserShareRiskAccountDao userShareRiskAccountDao;

    public UserShareRiskControlService(UserLoginAuditDao userLoginAuditDao,
                                       UserDao userDao,
                                       UserSessionHelper userSessionHelper,
                                       LoginAuditService loginAuditService,
                                       UserShareRiskPolicy userShareRiskPolicy,
                                       UserShareRiskAccountDao userShareRiskAccountDao) {
        this.userLoginAuditDao = userLoginAuditDao;
        this.userDao = userDao;
        this.userSessionHelper = userSessionHelper;
        this.loginAuditService = loginAuditService;
        this.userShareRiskPolicy = userShareRiskPolicy;
        this.userShareRiskAccountDao = userShareRiskAccountDao;
    }

    /**
     * 每天凌晨：扫描 audit → 刷新 user_share_risk_account → 决定自动禁/解禁。
     * <p>
     * 设计意图：admin 列表只读这张表；audit 表只是滚动日志，可以独立按周期清理。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void scanAndUpsertRiskAccountsBySchedule() {
        scanAndUpsertRiskAccounts();
    }

    public void scanAndUpsertRiskAccounts() {
        long start = System.currentTimeMillis();
        SearchUserShareRiskReq scanReq = buildScanReq();
        Long total = userLoginAuditDao.countUserShareRisk(scanReq);
        Set<Long> highRiskUserIds = new HashSet<>();

        if (total != null && total > 0) {
            for (long offset = 0; offset < total; offset += SCAN_BATCH_SIZE) {
                List<UserShareRiskDTO> batch = userLoginAuditDao.listUserShareRisk(scanReq, offset, SCAN_BATCH_SIZE);
                for (UserShareRiskDTO raw : batch) {
                    if (raw == null || raw.getUserId() == null) {
                        continue;
                    }
                    UserShareRiskAccountDO existed = userShareRiskAccountDao.getByUserId(raw.getUserId());
                    UserShareRiskDTO effective = refineByLastRelease(raw, existed);
                    if (effective == null) {
                        continue;
                    }
                    upsertScanRow(effective, existed);
                    if (isHighRisk(effective)) {
                        highRiskUserIds.add(raw.getUserId());
                    }
                }
            }
        }

        reconcileExpiredForbiddenAccounts();
        applyAutoForbidden(highRiskUserIds);
        log.info("scanAndUpsertRiskAccounts done, candidateTotal={}, highRiskCount={}, costMs={}",
                total == null ? 0 : total, highRiskUserIds.size(), System.currentTimeMillis() - start);
    }

    /**
     * 兼容旧调用入口：现在内部走完整扫描 + 自动管控。
     */
    public void syncAutoForbiddenStatus() {
        scanAndUpsertRiskAccounts();
    }

    public boolean isHighRiskUser(Long userId) {
        if (userId == null) {
            return false;
        }
        UserShareRiskAccountDO existed = userShareRiskAccountDao.getByUserId(userId);
        Date cutoff = effectiveCountCutoff(existed);
        UserShareRiskDTO risk = cutoff == null
                ? userLoginAuditDao.getUserShareRisk(userId, AUTO_CHECK_RECENT_DAYS)
                : userLoginAuditDao.getUserShareRiskAfter(userId, cutoff);
        return isHighRisk(risk);
    }

    public String getHighRiskLoginTip(Long userId) {
        return isHighRiskUser(userId) ? HIGH_RISK_LOGIN_TIP : null;
    }

    /**
     * 知识星球授权登录验证通过后解禁账号；60s 内对同一 userId 去重，避免高频回调写多条解禁审计。
     */
    public boolean releaseForbiddenByZsxqAuth(Long userId) {
        if (userId == null) {
            return false;
        }
        UserDO user = userDao.getUserByUserId(userId);
        if (!isForbidden(user)) {
            return false;
        }
        if (!acquireZsxqReleaseLock(userId)) {
            log.info("Skip duplicate zsxq release within {}s, userId={}", ZSXQ_RELEASE_LOCK_TTL_SECONDS, userId);
            return false;
        }
        String reason = UserShareRiskPolicy.ZSXQ_AUTH_UNFORBID_REASON
                + "；原禁用原因：" + StringUtils.defaultIfBlank(user.getForbidReason(), "未记录");
        releaseForbidden(user, reason, HandleAction.ZSXQ_UNFORBID);
        return true;
    }

    private void upsertScanRow(UserShareRiskDTO effective, UserShareRiskAccountDO existed) {
        UserShareRiskAccountDO target = existed != null ? existed : new UserShareRiskAccountDO();
        target.setUserId(effective.getUserId());
        target.setLoginName(Optional.ofNullable(effective.getLoginName())
                .orElseGet(() -> existed == null ? null : existed.getLoginName()));
        target.setStarNumber(Optional.ofNullable(effective.getStarNumber())
                .orElseGet(() -> existed == null ? null : existed.getStarNumber()));
        target.setKickoutCount(effective.getKickoutCount());
        target.setLoginSuccessCount(effective.getLoginSuccessCount());
        target.setDeviceCount(effective.getDeviceCount());
        target.setIpCount(effective.getIpCount());
        target.setLastKickoutTime(effective.getLastKickoutTime());
        target.setLastActiveTime(effective.getLastActiveTime());
        long kickout = nz(effective.getKickoutCount());
        long device = nz(effective.getDeviceCount());
        long ip = nz(effective.getIpCount());
        target.setRiskLevel(userShareRiskPolicy.resolveRiskLevel(kickout, device, ip));
        target.setRiskReason(userShareRiskPolicy.buildRiskReason(AUTO_CHECK_RECENT_DAYS, kickout, device, ip));
        target.setRecentDays(AUTO_CHECK_RECENT_DAYS);
        if (target.getForbidden() == null) {
            target.setForbidden(false);
        }
        // last_release_at / last_handle_reason 由处理动作决定，扫描不动；mapper 用 coalesce 保旧值
        target.setLastReleaseAt(null);
        target.setLastHandleReason(existed == null ? null : existed.getLastHandleReason());
        userShareRiskAccountDao.upsert(target);
    }

    private UserShareRiskDTO refineByLastRelease(UserShareRiskDTO raw, UserShareRiskAccountDO existed) {
        Date cutoff = effectiveCountCutoff(existed);
        if (cutoff == null) {
            return raw;
        }
        UserShareRiskDTO refined = userLoginAuditDao.getUserShareRiskAfter(raw.getUserId(), cutoff);
        if (refined == null) {
            return null;
        }
        if (StringUtils.isBlank(refined.getLoginName())) {
            refined.setLoginName(raw.getLoginName());
        }
        if (StringUtils.isBlank(refined.getStarNumber())) {
            refined.setStarNumber(raw.getStarNumber());
        }
        return refined;
    }

    /**
     * 计数窗口起点：取 max(last_release_at, 最近一次禁用到期时间)；如果都没有则返回 null 让上层走默认窗口。
     */
    private Date effectiveCountCutoff(UserShareRiskAccountDO existed) {
        if (existed == null) {
            return null;
        }
        Date release = existed.getLastReleaseAt();
        Date expiredForbid = existed.getForbidUntil();
        Date now = new Date();
        // 仅当 forbid_until 已过期时才作为"重新计数起点"
        if (expiredForbid != null && expiredForbid.after(now)) {
            expiredForbid = null;
        }
        if (release == null) {
            return expiredForbid;
        }
        if (expiredForbid == null) {
            return release;
        }
        return release.after(expiredForbid) ? release : expiredForbid;
    }

    private void applyAutoForbidden(Set<Long> highRiskUserIds) {
        highRiskUserIds.forEach(this::handleHighRiskUser);

        List<UserDO> autoForbiddenUsers = userDao.listUsersByForbidReason(UserShareRiskPolicy.AUTO_FORBID_REASON);
        for (UserDO user : autoForbiddenUsers) {
            if (user == null || user.getId() == null) {
                continue;
            }
            if (highRiskUserIds.contains(user.getId())) {
                continue;
            }
            releaseAutoForbidden(user);
        }
    }

    private void handleHighRiskUser(Long userId) {
        if (userId == null) {
            return;
        }
        UserDO user = userDao.getUserByUserId(userId);
        if (user == null) {
            return;
        }
        if (isManualForbidden(user)) {
            return;
        }
        if (isAutoForbiddenAndEffective(user)) {
            return;
        }

        Date now = new Date();
        Date forbidUntil = new Date(now.getTime() + UserShareRiskPolicy.AUTO_FORBID_DAYS * 24L * 60 * 60 * 1000);
        user.setForbidTime(now);
        user.setForbidUntil(forbidUntil);
        user.setForbidReason(UserShareRiskPolicy.AUTO_FORBID_REASON);
        user.setForbidOperatorId(SYSTEM_OPERATOR_ID);
        userDao.updateUserForbidden(userId, now, forbidUntil, UserShareRiskPolicy.AUTO_FORBID_REASON, SYSTEM_OPERATOR_ID);
        userSessionHelper.removeAllSessionsByUserId(userId, "ACCOUNT_SUSPENDED");

        UserShareRiskAccountDO existed = userShareRiskAccountDao.getByUserId(userId);
        Date cutoff = effectiveCountCutoff(existed);
        UserShareRiskDTO risk = cutoff == null
                ? userLoginAuditDao.getUserShareRisk(userId, AUTO_CHECK_RECENT_DAYS)
                : userLoginAuditDao.getUserShareRiskAfter(userId, cutoff);
        String riskDetail = risk == null ? "" : userShareRiskPolicy.buildRiskDetail(
                AUTO_CHECK_RECENT_DAYS,
                nz(risk.getKickoutCount()),
                nz(risk.getDeviceCount()),
                nz(risk.getIpCount()));
        loginAuditService.recordAccountForbid(user, UserShareRiskPolicy.AUTO_FORBID_REASON + "；" + riskDetail, HandleAction.AUTO_FORBID);
        log.info("Auto forbid shared-risk user, userId={}, loginName={}", userId, user.getUserName());
    }

    /**
     * 巡检发现 user_share_risk_account.forbidden=true 但 user.forbid_until 已过期 → 同步成"未禁用"。
     */
    private void reconcileExpiredForbiddenAccounts() {
        List<UserShareRiskAccountDO> list = userShareRiskAccountDao.lambdaQuery()
                .eq(UserShareRiskAccountDO::getForbidden, true)
                .list();
        Date now = new Date();
        for (UserShareRiskAccountDO account : list) {
            UserDO user = userDao.getUserByUserId(account.getUserId());
            if (user == null) {
                continue;
            }
            if (user.getForbidUntil() != null && user.getForbidUntil().after(now)) {
                continue;
            }
            account.setForbidden(false);
            account.setLastReleaseAt(maxDate(account.getLastReleaseAt(), account.getForbidUntil()));
            account.setForbidUntil(null);
            account.setForbidReason(null);
            if (UserShareRiskPolicy.FORBIDDEN_ONLY_RISK_REASON.equals(account.getRiskReason())) {
                account.setRiskReason(null);
            }
            userShareRiskAccountDao.upsert(account);
        }
    }

    private void releaseAutoForbidden(UserDO user) {
        if (!userShareRiskPolicy.isAutoForbiddenReason(user.getForbidReason())) {
            return;
        }
        releaseForbidden(user, UserShareRiskPolicy.AUTO_UNFORBID_REASON, HandleAction.AUTO_UNFORBID);
    }

    private void releaseForbidden(UserDO user, String reason, HandleAction action) {
        if (user == null || user.getId() == null) {
            return;
        }
        user.setForbidTime(null);
        user.setForbidUntil(null);
        user.setForbidReason(null);
        user.setForbidOperatorId(SYSTEM_OPERATOR_ID);
        userDao.clearUserForbidden(user.getId(), SYSTEM_OPERATOR_ID);
        loginAuditService.recordAccountUnforbid(user, reason, action);
        log.info("Unforbid shared-risk user, userId={}, loginName={}, action={}", user.getId(), user.getUserName(), action);
    }

    private boolean acquireZsxqReleaseLock(Long userId) {
        try {
            Boolean ok = RedisClient.setStrIfAbsentWithExpire(
                    ZSXQ_RELEASE_LOCK_KEY_PREFIX + userId,
                    String.valueOf(System.currentTimeMillis()),
                    ZSXQ_RELEASE_LOCK_TTL_SECONDS);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("acquireZsxqReleaseLock failed, fallback to allow, userId={}", userId, e);
            return true;
        }
    }

    private SearchUserShareRiskReq buildScanReq() {
        SearchUserShareRiskReq req = new SearchUserShareRiskReq();
        req.setRecentDays(AUTO_CHECK_RECENT_DAYS);
        req.setMinKickoutCount(SCAN_MIN_KICKOUT);
        req.setMinDeviceCount(SCAN_MIN_DEVICE);
        req.setMinIpCount(SCAN_MIN_IP);
        return req;
    }

    private boolean isHighRisk(UserShareRiskDTO dto) {
        if (dto == null) {
            return false;
        }
        return userShareRiskPolicy.isHighRisk(nz(dto.getKickoutCount()), nz(dto.getDeviceCount()), nz(dto.getIpCount()));
    }

    private boolean isManualForbidden(UserDO user) {
        return user != null
                && user.getForbidUntil() != null
                && user.getForbidUntil().after(new Date())
                && !userShareRiskPolicy.isAutoForbiddenReason(user.getForbidReason());
    }

    private boolean isAutoForbiddenAndEffective(UserDO user) {
        return user != null
                && userShareRiskPolicy.isAutoForbiddenReason(user.getForbidReason())
                && user.getForbidUntil() != null
                && user.getForbidUntil().after(new Date());
    }

    private boolean isForbidden(UserDO user) {
        return user != null
                && user.getForbidUntil() != null
                && user.getForbidUntil().after(new Date());
    }

    private long nz(Long value) {
        return value == null ? 0L : value;
    }

    private Date maxDate(Date a, Date b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.after(b) ? a : b;
    }
}
