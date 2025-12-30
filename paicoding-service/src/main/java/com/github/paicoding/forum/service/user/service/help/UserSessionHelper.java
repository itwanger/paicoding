package com.github.paicoding.forum.service.user.service.help;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.paicoding.forum.core.cache.RedisClient;
import org.springframework.data.redis.core.RedisCallback;
import com.github.paicoding.forum.core.mdc.SelfTraceIdGenerator;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * 使用jwt来存储用户token，则不需要后端来存储session了
 *
 * @author YiHui
 * @date 2022/12/5
 */
@Slf4j
@Component
public class UserSessionHelper {
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

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    public UserSessionHelper(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        verifier = JWT.require(algorithm).withIssuer(jwtProperties.getIssuer()).build();
    }

    public String genSession(Long userId) {
        // 1.生成jwt格式的会话，内部持有有效期，用户信息
        String session = JsonUtil.toStr(MapUtils.create("s", SelfTraceIdGenerator.generate(), "u", userId));
        String token = JWT.create().withIssuer(jwtProperties.getIssuer()).withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getExpire()))
                .withPayload(session)
                .sign(algorithm);

        // 2.使用jwt生成的token时，后端可以不存储这个session信息, 完全依赖jwt的信息
        // 但是需要考虑到用户登出，需要主动失效这个token，而jwt本身无状态，所以再这里的redis做一个简单的token -> userId的缓存，用于双重判定
        RedisClient.setStrWithExpire(token, String.valueOf(userId), jwtProperties.getExpire() / 1000);
        return token;
    }

    public void removeSession(String session) {
        RedisClient.del(session);
    }

    /**
     * 根据用户ID删除其所有session，实现踢人下线功能
     *
     * @param userId 用户ID
     */
    public void removeAllSessionsByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        String targetUserId = String.valueOf(userId);

        // 使用 SCAN 命令遍历所有以 pai_ 开头的 key
        // 检查 value 是否匹配目标 userId，如果匹配则删除该 session
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match("pai_*")
                    .count(100)
                    .build();

            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                try {
                    // 检查 key 类型，只处理 String 类型的 key（session token）
                    DataType dataType = connection.type(key);
                    if (dataType != DataType.STRING) {
                        continue;
                    }

                    byte[] value = connection.get(key);

                    if (value != null) {
                        String userIdInRedis = new String(value, StandardCharsets.UTF_8);
                        if (targetUserId.equals(userIdInRedis)) {
                            // 删除匹配的 session token
                            connection.keyCommands().del(key);
                            log.info("踢掉用户 {} 的 session，key: {}", userId, new String(key, StandardCharsets.UTF_8));
                        }
                    }
                } catch (Exception e) {
                    // 跳过处理出错或类型不匹配的 key
                    log.debug("处理 key 时出错，跳过: {}", new String(key, StandardCharsets.UTF_8), e);
                }
            }
            return null;
        });
    }

    /**
     * 根据会话获取用户信息
     *
     * @param session
     * @return
     */
    public Long getUserIdBySession(String session) {
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
            return Long.valueOf(user);
        } catch (Exception e) {
            log.debug("jwt token校验失败! token: {}, msg: {}", session, e.getMessage());
            // 如果jwt过期，自动删除用户的cookie；主要是为了解决jwt的有效期与cookie有效期不一致的场景
            SessionUtil.delCookies(LoginService.SESSION_KEY);
            return null;
        }
    }
}
