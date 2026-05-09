package com.github.paicoding.forum.service.user.service.help;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.mdc.SelfTraceIdGenerator;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.conf.LoginRiskProperties;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 使用jwt来存储用户token，则不需要后端来存储session了
 *
 * @author YiHui
 * @date 2022/12/5
 */
@Slf4j
@Component
public class UserSessionHelper {
    private static final String USER_SESSION_PREFIX = "user_sessions:";
    private static final String SESSION_META_PREFIX = "session_meta:";
    private static final String SESSION_TOUCH_PREFIX = "session_touch:";
    private static final String USER_LOGOUT_REASON = "USER_LOGOUT";
    private static final String DEVICE_LIMIT_KICKOUT_REASON = "DEVICE_LIMIT_KICKOUT";
    private static final String FORCE_KICKOUT_REASON = "FORCE_KICKOUT";
    private static final String ACCOUNT_SUSPENDED_REASON = "ACCOUNT_SUSPENDED";
    private static final DateTimeFormatter FORBID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Component
    @Data
    @ConfigurationProperties("paicoding.jwt")
    public static class JwtProperties {
        /**
         * 签发人
         */
        private String issuer;
        /**
         * 密钥
         */
        private String secret;
        /**
         * 有效期，毫秒时间戳
         */
        private Long expire;
    }

    @Data
    public static class SessionDeviceMeta {
        private Long userId;
        private String loginName;
        private String starNumber;
        private Integer loginType;
        private String deviceId;
        private String deviceName;
        private String userAgent;
        private String uaHash;
        private String ip;
        private String region;
        private Long loginTime;
        private Long latestSeenTime;
        private Long expireTime;
    }

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final LoginRiskProperties loginRiskProperties;
    private final LoginAuditService loginAuditService;
    private final UserDao userDao;
    private final UserAiDao userAiDao;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    public UserSessionHelper(JwtProperties jwtProperties,
                             RedisTemplate<String, String> redisTemplate,
                             LoginRiskProperties loginRiskProperties,
                             LoginAuditService loginAuditService,
                             UserDao userDao,
                             UserAiDao userAiDao) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        this.loginRiskProperties = loginRiskProperties;
        this.loginAuditService = loginAuditService;
        this.userDao = userDao;
        this.userAiDao = userAiDao;
        algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        verifier = JWT.require(algorithm).withIssuer(jwtProperties.getIssuer()).build();
    }

    public String genSession(Long userId) {
        return genSession(userId, null, null);
    }

    public String genSession(Long userId, String loginName, Integer loginType) {
        // 1.生成jwt格式的会话，内部持有有效期，用户信息
        long now = System.currentTimeMillis();
        long expireTime = now + jwtProperties.getExpire();
        String session = JsonUtil.toStr(MapUtils.create("s", SelfTraceIdGenerator.generate(), "u", userId));
        String token = JWT.create().withIssuer(jwtProperties.getIssuer()).withExpiresAt(new Date(expireTime))
                .withPayload(session)
                .sign(algorithm);

        SessionDeviceMeta sessionMeta = buildSessionMeta(userId, loginName, loginType, now, expireTime);
        assertUserLoginAllowed(userId, loginName, loginType, sessionMeta);
        String riskTag = enforceDeviceLimit(userId, sessionMeta);

        // 2.使用jwt生成的token时，后端可以不存储这个session信息, 完全依赖jwt的信息
        // 但是需要考虑到用户登出，需要主动失效这个token，而jwt本身无状态，所以再这里的redis做一个简单的token -> userId的缓存，用于双重判定
        long expireSeconds = jwtProperties.getExpire() / 1000;
        RedisClient.setStrWithExpire(token, String.valueOf(userId), expireSeconds);
        RedisClient.setStrWithExpire(sessionMetaKey(token), JsonUtil.toStr(sessionMeta), expireSeconds);

        // 3.维护用户的 session 索引，用于快速踢人下线
        String userSessionKey = userSessionKey(userId);
        redisTemplate.opsForSet().add(userSessionKey, token);
        redisTemplate.expire(userSessionKey, Duration.ofSeconds(expireSeconds));

        String sessionHash = buildSessionHash(token);
        loginAuditService.upsertActiveSession(sessionMeta, sessionHash);
        loginAuditService.recordLoginSuccess(sessionMeta, sessionHash, riskTag);
        userDao.updateLastLoginTime(userId, new Date(now));

        return token;
    }

    public void removeSession(String session) {
        removeSession(session, FORCE_KICKOUT_REASON);
    }

    public void removeSession(String session, String reason) {
        if (StringUtils.isBlank(session)) {
            return;
        }

        // 先获取 userId 以便从索引中移除
        String userId = RedisClient.getStr(session);
        SessionDeviceMeta meta = getSessionMeta(session);
        if (userId != null) {
            String userSessionKey = userSessionKey(Long.valueOf(userId));
            redisTemplate.opsForSet().remove(userSessionKey, session);
        }
        RedisClient.del(session);
        RedisClient.del(sessionMetaKey(session));
        RedisClient.del(sessionTouchKey(session));

        if (meta == null && userId != null) {
            meta = new SessionDeviceMeta();
            meta.setUserId(Long.valueOf(userId));
        }
        loginAuditService.recordSessionOffline(buildSessionHash(session), reason, meta, !Objects.equals(reason, USER_LOGOUT_REASON));
    }

    public void logout(String session) {
        removeSession(session, USER_LOGOUT_REASON);
    }

    /**
     * 根据用户ID删除其所有session，实现踢人下线功能
     *
     * @param userId 用户ID
     */
    public void removeAllSessionsByUserId(Long userId) {
        removeAllSessionsByUserId(userId, FORCE_KICKOUT_REASON);
    }

    public void removeAllSessionsByUserId(Long userId, String reason) {
        if (userId == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String userSessionKey = "user_sessions:" + userId;

        // 优先从用户的 session 索引中获取所有 session（新版本）
        Set<String> sessions = redisTemplate.opsForSet().members(userSessionKey);
        long queryDuration = System.currentTimeMillis() - startTime;

        // 兼容旧版本：如果索引不存在，回退到 SCAN 方式
        if (sessions == null || sessions.isEmpty()) {
            log.warn("用户 {} 的 session 索引不存在，使用 SCAN 方式兜底（可能是旧版本创建的 session）", userId);
            removeAllSessionsByUserIdLegacy(userId, reason);
            return;
        }

        log.info("查询用户 {} 的 session 索引耗时: {}ms, 数量: {}", userId, queryDuration, sessions.size());

        // 删除所有 session token
        for (String session : sessions) {
            removeSession(session, reason);
            log.info("踢掉用户 {} 的 session，key: {}", userId, session);
        }

        // 删除用户的 session 索引
        redisTemplate.delete(userSessionKey);

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("用户 {} 踢人下线总耗时: {}ms, session 数量: {}", userId, totalDuration, sessions.size());
    }

    /**
     * 旧版本踢人下线实现（使用 SCAN 全表扫描）
     * 保留用于向后兼容，处理旧版本创建的 session
     *
     * @param userId 用户ID
     * @deprecated 仅为兼容旧版本 session，新版本使用索引查询
     */
    @Deprecated
    private void removeAllSessionsByUserIdLegacy(Long userId, String reason) {
        String targetUserId = String.valueOf(userId);
        List<String> sessions = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match("pai_*")
                    .count(100)
                    .build();

            List<String> result = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                try {
                    DataType dataType = connection.type(key);
                    if (dataType != DataType.STRING) {
                        continue;
                    }

                    byte[] value = connection.get(key);
                    if (value != null) {
                        String userIdInRedis = new String(value, StandardCharsets.UTF_8);
                        if (targetUserId.equals(userIdInRedis)) {
                            result.add(new String(key, StandardCharsets.UTF_8).replaceFirst("^pai_", ""));
                        }
                    }
                } catch (Exception e) {
                    log.debug("处理 key 时出错，跳过: {}", new String(key, StandardCharsets.UTF_8), e);
                }
            }
            return result;
        });

        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (String session : sessions) {
            removeSession(session, reason);
            log.info("踢掉用户 {} 的 session（旧版本），key: {}", userId, session);
        }
        log.info("用户 {} 通过 SCAN 方式踢掉了 {} 个 session", userId, sessions.size());
    }

    /**
     * 根据会话获取用户信息
     *
     * @param session
     * @return
     */
    public Long getUserIdBySession(String session) {
        return getUserIdBySession(session, null, null, null);
    }

    public Long getUserIdBySession(String session, String clientIp, String deviceId, String userAgent) {
        // jwt的校验方式，如果token非法或者过期，则直接验签失败
        try {
            DecodedJWT decodedJWT = verifier.verify(session);
            String pay = new String(Base64Utils.decodeFromString(decodedJWT.getPayload()));
            // jwt验证通过，获取对应的userId
            String userId = String.valueOf(JsonUtil.toObj(pay, HashMap.class).get("u"));

            // 从redis中获取userId，解决用户登出，后台失效jwt token的问题
            String user = RedisClient.getStr(session);
            if (user == null || !Objects.equals(userId, user)) {
                return null;
            }
            if (isUserForbidden(Long.valueOf(userId))) {
                removeSession(session, ACCOUNT_SUSPENDED_REASON);
                return null;
            }
            refreshSessionMeta(session, Long.valueOf(userId), clientIp, deviceId, userAgent);
            return Long.valueOf(user);
        } catch (Exception e) {
            log.debug("jwt token校验失败! token: {}, msg: {}", session, e.getMessage());
            // 如果jwt过期，自动删除用户的cookie；主要是为了解决jwt的有效期与cookie有效期不一致的场景
            try {
                SessionUtil.delCookies(LoginService.SESSION_KEY);
            } catch (Exception ignore) {
                log.debug("删除过期cookie失败，忽略");
            }
            return null;
        }
    }

    private String enforceDeviceLimit(Long userId, SessionDeviceMeta pendingMeta) {
        Integer maxActiveDevices = Optional.ofNullable(loginRiskProperties.getMaxActiveDevices()).orElse(0);
        if (maxActiveDevices <= 0) {
            return null;
        }

        Set<String> sessions = redisTemplate.opsForSet().members(userSessionKey(userId));
        if (sessions == null || sessions.isEmpty()) {
            return "NEW_DEVICE";
        }

        Map<String, List<String>> deviceSessions = new HashMap<>();
        Map<String, Long> deviceLoginTime = new HashMap<>();
        List<String> staleSessions = new ArrayList<>();
        for (String session : sessions) {
            SessionDeviceMeta meta = getSessionMeta(session);
            String bindUserId = RedisClient.getStr(session);
            if (meta == null || bindUserId == null || !Objects.equals(String.valueOf(userId), bindUserId)) {
                staleSessions.add(session);
                continue;
            }

            String existDeviceId = normalizeDeviceId(meta.getDeviceId(), meta.getUaHash(), session);
            deviceSessions.computeIfAbsent(existDeviceId, key -> new ArrayList<>()).add(session);
            deviceLoginTime.merge(existDeviceId, Optional.ofNullable(meta.getLoginTime()).orElse(Long.MAX_VALUE), Math::min);
        }

        if (!staleSessions.isEmpty()) {
            redisTemplate.opsForSet().remove(userSessionKey(userId), staleSessions.toArray(new String[0]));
        }

        String deviceId = normalizeDeviceId(pendingMeta.getDeviceId(), pendingMeta.getUaHash(), pendingMeta.getUserId() == null ? null : String.valueOf(pendingMeta.getUserId()));
        if (deviceSessions.containsKey(deviceId)) {
            return "KNOWN_DEVICE";
        }
        if (deviceSessions.size() < maxActiveDevices) {
            return "NEW_DEVICE";
        }

        String oldestDeviceId = deviceLoginTime.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (oldestDeviceId == null) {
            return "NEW_DEVICE";
        }

        for (String session : deviceSessions.getOrDefault(oldestDeviceId, Collections.emptyList())) {
            removeSession(session, DEVICE_LIMIT_KICKOUT_REASON);
        }
        return "DEVICE_LIMIT_REPLACED";
    }

    private void refreshSessionMeta(String session, Long userId, String clientIp, String deviceId, String userAgent) {
        SessionDeviceMeta meta = Optional.ofNullable(getSessionMeta(session))
                .orElseGet(() -> buildSessionMeta(userId, null, null, System.currentTimeMillis(), System.currentTimeMillis() + jwtProperties.getExpire()));

        meta.setUserId(userId);
        if (StringUtils.isNotBlank(deviceId)) {
            meta.setDeviceId(deviceId);
        }
        if (StringUtils.isNotBlank(userAgent)) {
            meta.setUserAgent(userAgent);
            meta.setUaHash(Md5Util.encode(userAgent));
            meta.setDeviceName(buildDeviceName(userAgent));
        }
        if (StringUtils.isNotBlank(clientIp)) {
            meta.setIp(clientIp);
            meta.setRegion(resolveRegion(clientIp));
        }
        meta.setLatestSeenTime(System.currentTimeMillis());

        Long ttl = RedisClient.ttl(session);
        if (ttl != null && ttl > 0) {
            RedisClient.setStrWithExpire(sessionMetaKey(session), JsonUtil.toStr(meta), ttl);
        } else {
            RedisClient.setStr(sessionMetaKey(session), JsonUtil.toStr(meta));
        }

        if (Boolean.TRUE.equals(RedisClient.setStrIfAbsentWithExpire(sessionTouchKey(session), "1",
                Optional.ofNullable(loginRiskProperties.getTouchSyncSeconds()).orElse(300).longValue()))) {
            loginAuditService.touchActiveSession(buildSessionHash(session), meta);
        }
    }

    private SessionDeviceMeta buildSessionMeta(Long userId, String loginName, Integer loginType, long loginTime, long expireTime) {
        SessionDeviceMeta meta = new SessionDeviceMeta();
        meta.setUserId(userId);
        meta.setLoginName(loginName);
        meta.setStarNumber(resolveStarNumber(userId, loginName));
        meta.setLoginType(loginType);
        meta.setLoginTime(loginTime);
        meta.setLatestSeenTime(loginTime);
        meta.setExpireTime(expireTime);

        ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getReqInfo();
        if (reqInfo == null) {
            return meta;
        }

        meta.setDeviceId(reqInfo.getDeviceId());
        meta.setUserAgent(reqInfo.getUserAgent());
        meta.setUaHash(StringUtils.isBlank(reqInfo.getUserAgent()) ? null : Md5Util.encode(reqInfo.getUserAgent()));
        meta.setDeviceName(buildDeviceName(reqInfo.getUserAgent()));
        meta.setIp(reqInfo.getClientIp());
        meta.setRegion(resolveRegion(reqInfo.getClientIp()));
        return meta;
    }

    private SessionDeviceMeta getSessionMeta(String session) {
        String meta = RedisClient.getStr(sessionMetaKey(session));
        if (StringUtils.isBlank(meta)) {
            return null;
        }
        try {
            return JsonUtil.toObj(meta, SessionDeviceMeta.class);
        } catch (Exception e) {
            log.warn("解析session元数据失败: {}", session, e);
            return null;
        }
    }

    private String resolveRegion(String clientIp) {
        if (StringUtils.isBlank(clientIp)) {
            return null;
        }
        try {
            return IpUtil.getLocationByIp(clientIp).toRegionStr();
        } catch (Exception e) {
            log.debug("解析IP归属地失败: {}", clientIp, e);
            return null;
        }
    }

    private String normalizeDeviceId(String deviceId, String uaHash, String fallback) {
        if (StringUtils.isNotBlank(deviceId)) {
            return deviceId;
        }
        if (StringUtils.isNotBlank(uaHash)) {
            return "ua:" + uaHash;
        }
        return "unknown:" + fallback;
    }

    private String buildDeviceName(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "Unknown";
        }
        return resolveOs(userAgent) + " / " + resolveBrowser(userAgent);
    }

    private String resolveOs(String userAgent) {
        if (StringUtils.containsIgnoreCase(userAgent, "Windows")) {
            return "Windows";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Mac OS X")) {
            return "macOS";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Android")) {
            return "Android";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "iPhone") || StringUtils.containsIgnoreCase(userAgent, "iPad")) {
            return "iOS";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Linux")) {
            return "Linux";
        }
        return "UnknownOS";
    }

    private String resolveBrowser(String userAgent) {
        if (StringUtils.containsIgnoreCase(userAgent, "Edg/")) {
            return "Edge";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Chrome/")) {
            return "Chrome";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Firefox/")) {
            return "Firefox";
        }
        if (StringUtils.containsIgnoreCase(userAgent, "Safari/")) {
            return "Safari";
        }
        return "UnknownBrowser";
    }

    private String userSessionKey(Long userId) {
        return USER_SESSION_PREFIX + userId;
    }

    private String sessionMetaKey(String session) {
        return SESSION_META_PREFIX + session;
    }

    private String sessionTouchKey(String session) {
        return SESSION_TOUCH_PREFIX + buildSessionHash(session);
    }

    private String buildSessionHash(String session) {
        return Md5Util.encode(session);
    }

    private String resolveStarNumber(Long userId, String loginName) {
        if (userId != null) {
            UserAiDO userAi = userAiDao.getByUserId(userId);
            if (userAi != null && StringUtils.isNotBlank(userAi.getStarNumber())) {
                return userAi.getStarNumber();
            }
            UserDO user = userDao.getUserByUserId(userId);
            if (user != null && StringUtils.startsWith(user.getUserName(), "zsxq_")) {
                return StringUtils.substringAfter(user.getUserName(), "zsxq_");
            }
        }
        if (StringUtils.startsWith(loginName, "zsxq_")) {
            return StringUtils.substringAfter(loginName, "zsxq_");
        }
        return null;
    }

    private void assertUserLoginAllowed(Long userId, String loginName, Integer loginType, SessionDeviceMeta sessionMeta) {
        UserDO user = userDao.getUserByUserId(userId);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userId);
        }
        if (!isForbidden(user)) {
            return;
        }

        String untilText = formatForbidUntil(user.getForbidUntil());
        String msg = untilText + (StringUtils.isBlank(user.getForbidReason()) ? "" : "，原因：" + user.getForbidReason());
        loginAuditService.recordLoginFail(StringUtils.defaultIfBlank(loginName, user.getUserName()), loginType, "账号已禁用:" + msg, sessionMeta);
        throw ExceptionUtil.of(StatusEnum.USER_FORBID_LOGIN, msg);
    }

    private boolean isUserForbidden(Long userId) {
        UserDO user = userDao.getUserByUserId(userId);
        return isForbidden(user);
    }

    private boolean isForbidden(UserDO user) {
        return user != null && user.getForbidUntil() != null && user.getForbidUntil().after(new Date());
    }

    private String formatForbidUntil(Date forbidUntil) {
        if (forbidUntil == null) {
            return "禁用中";
        }
        return "截止至 " + FORBID_TIME_FORMATTER.format(forbidUntil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
}
