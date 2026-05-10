package com.github.paicoding.forum.service.user.service.audit;

import com.github.paicoding.forum.api.model.vo.user.dto.UserShareRiskDTO;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserLoginAuditDao;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 疑似共享账号自动管控
 *
 * @author Codex
 * @date 2026/5/10
 */
@Slf4j
@Service
public class UserShareRiskControlService {
    private static final int AUTO_CHECK_RECENT_DAYS = 7;
    private static final Long SYSTEM_OPERATOR_ID = 0L;
    public static final String HIGH_RISK_LOGIN_TIP = "账号近期存在频繁切换IP/设备的风险记录，请尽量固定常用设备和网络登录";

    private final UserLoginAuditDao userLoginAuditDao;
    private final UserDao userDao;
    private final UserSessionHelper userSessionHelper;
    private final LoginAuditService loginAuditService;
    private final UserShareRiskPolicy userShareRiskPolicy;

    public UserShareRiskControlService(UserLoginAuditDao userLoginAuditDao,
                                       UserDao userDao,
                                       UserSessionHelper userSessionHelper,
                                       LoginAuditService loginAuditService,
                                       UserShareRiskPolicy userShareRiskPolicy) {
        this.userLoginAuditDao = userLoginAuditDao;
        this.userDao = userDao;
        this.userSessionHelper = userSessionHelper;
        this.loginAuditService = loginAuditService;
        this.userShareRiskPolicy = userShareRiskPolicy;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void syncAutoForbiddenStatusBySchedule() {
        syncAutoForbiddenStatus();
    }

    public void syncAutoForbiddenStatus() {
        List<UserShareRiskDTO> highRiskList = userLoginAuditDao.listHighRiskUsers(AUTO_CHECK_RECENT_DAYS);
        Map<Long, UserShareRiskDTO> highRiskMap = OptionalHelper.toMap(highRiskList, UserShareRiskDTO::getUserId);

        highRiskList.forEach(this::handleHighRiskUser);

        List<UserDO> autoForbiddenUsers = userDao.listUsersByForbidReason(UserShareRiskPolicy.AUTO_FORBID_REASON);
        for (UserDO user : autoForbiddenUsers) {
            if (user == null || user.getId() == null) {
                continue;
            }
            if (highRiskMap.containsKey(user.getId())) {
                continue;
            }
            releaseAutoForbidden(user);
        }
    }

    public boolean isHighRiskUser(Long userId) {
        if (userId == null) {
            return false;
        }
        UserShareRiskDTO riskDTO = userLoginAuditDao.getUserShareRisk(userId, AUTO_CHECK_RECENT_DAYS);
        if (riskDTO == null) {
            return false;
        }
        return userShareRiskPolicy.isHighRisk(
                safeLong(riskDTO.getKickoutCount()),
                safeLong(riskDTO.getDeviceCount()),
                safeLong(riskDTO.getIpCount()));
    }

    public String getHighRiskLoginTip(Long userId) {
        return isHighRiskUser(userId) ? HIGH_RISK_LOGIN_TIP : null;
    }

    private void handleHighRiskUser(UserShareRiskDTO riskDTO) {
        Long userId = riskDTO == null ? null : riskDTO.getUserId();
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

        String riskReason = StringUtils.defaultIfBlank(riskDTO.getRiskReason(),
                userShareRiskPolicy.buildRiskDetail(AUTO_CHECK_RECENT_DAYS,
                        safeLong(riskDTO.getKickoutCount()),
                        safeLong(riskDTO.getDeviceCount()),
                        safeLong(riskDTO.getIpCount())));
        loginAuditService.recordAccountForbid(user, UserShareRiskPolicy.AUTO_FORBID_REASON + "；" + riskReason);
        log.info("Auto forbid shared-risk user, userId={}, starNumber={}, loginName={}", userId, riskDTO.getStarNumber(), riskDTO.getLoginName());
    }

    private void releaseAutoForbidden(UserDO user) {
        if (user == null || user.getId() == null || !userShareRiskPolicy.isAutoForbiddenReason(user.getForbidReason())) {
            return;
        }

        user.setForbidTime(null);
        user.setForbidUntil(null);
        user.setForbidReason(null);
        user.setForbidOperatorId(SYSTEM_OPERATOR_ID);
        userDao.clearUserForbidden(user.getId(), SYSTEM_OPERATOR_ID);
        loginAuditService.recordAccountUnforbid(user, UserShareRiskPolicy.AUTO_UNFORBID_REASON);
        log.info("Auto unforbid shared-risk user, userId={}, loginName={}", user.getId(), user.getUserName());
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

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private static final class OptionalHelper {
        private OptionalHelper() {
        }

        private static <T, K> Map<K, T> toMap(List<T> list, Function<T, K> keyGetter) {
            if (list == null || list.isEmpty()) {
                return Collections.emptyMap();
            }
            return list.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> keyGetter.apply(item) != null)
                    .collect(Collectors.toMap(keyGetter, Function.identity(), (left, right) -> left));
        }
    }
}
