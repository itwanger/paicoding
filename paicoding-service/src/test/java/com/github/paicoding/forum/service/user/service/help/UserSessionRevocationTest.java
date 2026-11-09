package com.github.paicoding.forum.service.user.service.help;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.conf.LoginRiskProperties;
import com.github.paicoding.forum.service.user.service.whitelist.ZsxqWhiteListServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionRevocationTest {
    @Mock private UserDao users;
    @Mock private UserAiDao whitelistDao;
    @Mock private LoginAuditService audit;
    @Mock private RedisTemplate<String, String> redis;
    @Mock private SetOperations<String, String> sets;
    @Mock private RedisConnection connection;

    private RedisTemplate<String, String> previousRedis;
    private UserSessionHelper sessions;
    private ZsxqWhiteListServiceImpl whitelist;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        previousRedis = (RedisTemplate<String, String>) ReflectionTestUtils.getField(RedisClient.class, "template");
        RedisClient.register(redis);
        when(redis.opsForSet()).thenReturn(sets);
        lenient().when(redis.execute(any(RedisCallback.class))).thenAnswer(call ->
                ((RedisCallback<?>) call.getArgument(0)).doInRedis(connection));
        UserSessionHelper.JwtProperties jwt = new UserSessionHelper.JwtProperties();
        jwt.setIssuer("test");
        jwt.setSecret("session-revocation-test");
        jwt.setExpire(60000L);
        sessions = new UserSessionHelper(jwt, redis, new LoginRiskProperties(), audit, users, whitelistDao);
        whitelist = new ZsxqWhiteListServiceImpl();
        ReflectionTestUtils.setField(whitelist, "userAiDao", whitelistDao);
        ReflectionTestUtils.setField(whitelist, "userSessionHelper", sessions);
    }

    @AfterEach
    void restoreRedis() {
        RedisClient.register(previousRedis);
    }

    @Test
    void rejectExpiredAccountWithoutSessionsOnlyReadsItsIndex() {
        UserAiDO account = account(9913L, 11547L);
        when(whitelistDao.getById(9913L)).thenReturn(account);
        whitelist.operate(9913L, UserAIStatEnum.NOT_PASS);

        verify(sets).members("user_sessions:11547");
        verifyNoMoreInteractions(sets);
        verifyNoInteractions(connection);
        verify(users).incrementSessionVersion(11547L);
        assertThat(account.getState()).isEqualTo(3);
        verify(whitelistDao).updateById(account);
    }

    @Test
    void revokeUnindexedLegacySessionAndAcceptNewLogin() {
        UserDO user = new UserDO();
        user.setSessionVersion(0L);
        when(users.getUserByUserId(7L)).thenReturn(user);
        doAnswer(call -> {
            user.setSessionVersion(user.getSessionVersion() + 1);
            return null;
        }).when(users).incrementSessionVersion(7L);

        String legacy = JWT.create().withIssuer("test").withClaim("u", 7L)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256("session-revocation-test"));
        when(connection.get(RedisClient.keyBytes(legacy))).thenReturn(RedisClient.valBytes("7"));
        assertThat(sessions.getUserIdBySession(legacy)).isEqualTo(7L);

        String hash = Md5Util.encode(legacy);
        when(audit.getActiveSessionHashes(7L)).thenReturn(Collections.singleton(hash), Collections.emptySet());
        sessions.removeAllSessionsByUserId(7L);
        verify(audit).recordSessionOffline(hash, "FORCE_KICKOUT", null, true);
        assertThat(sessions.getUserIdBySession(legacy)).isNull();

        String fresh = sessions.genSession(7L);
        assertThat(JWT.decode(fresh).getClaim("sv").asLong()).isEqualTo(1L);
        when(connection.get(RedisClient.keyBytes(fresh))).thenReturn(RedisClient.valBytes("7"));
        assertThat(sessions.getUserIdBySession(fresh)).isEqualTo(7L);
        sessions.removeAllSessionsByUserId(7L);
        assertThat(sessions.getUserIdBySession(fresh)).isNull();
        verify(connection, never()).scan(any());
    }

    @Test
    void batchRejectionCleansOnlySelectedAccountsAndAuditsIndexedTokenOnce() {
        List<Long> ids = Arrays.asList(20L, 21L);
        when(whitelistDao.listByIds(ids)).thenReturn(Arrays.asList(account(20L, 7L), account(21L, 8L)));
        String token = "indexed-token";
        String hash = Md5Util.encode(token);
        when(sets.members("user_sessions:7")).thenReturn(Collections.singleton(token));
        when(connection.get(RedisClient.keyBytes(token))).thenReturn(RedisClient.valBytes("7"));
        when(audit.getActiveSessionHashes(7L)).thenReturn(Collections.singleton(hash));
        whitelist.batchOperate(ids, UserAIStatEnum.NOT_PASS);

        verify(whitelistDao).batchUpdateState(ids, 3);
        verify(users).incrementSessionVersion(7L);
        verify(users).incrementSessionVersion(8L);
        verifyNoMoreInteractions(users);
        verify(connection).del(RedisClient.keyBytes(token));
        verify(audit, times(1)).recordSessionOffline(eq(hash), eq("FORCE_KICKOUT"), any(), eq(true));
        verify(redis, never()).delete(anyString());
        verify(connection, never()).scan(any());
    }

    private UserAiDO account(long id, long userId) {
        UserAiDO account = new UserAiDO();
        account.setId(id);
        account.setUserId(userId);
        account.setState(UserAIStatEnum.EXPIRED.getCode());
        return account;
    }
}
