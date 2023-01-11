package com.github.liuyueyi.forum.service.user.service.help;

import com.github.liueyueyi.forum.api.model.exception.NoVlaInGuavaException;
import com.github.liuyueyi.forum.core.util.CodeGenerateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2022/12/5
 */
@Component
public class UserSessionHelper {
    /**
     * key = 验证码 value = userId
     */
    @Getter
    private LoadingCache<String, Long> codeUserIdCache;
    /**
     * key = session, value = userId
     */
    @Getter
    private LoadingCache<String, Long> sessionMap;


    /**
     * todo 知识点：bean完成之后的初始化方式，除了 @PostConstruct 之外还有构造方法方式、实现BeanPostProcessor接口方式
     */
    @PostConstruct
    public void init() {
        // 五分钟内，最多只支持300个用户登录；注意当服务多台机器部署时，基于本地缓存会有问题；请改成redis/memcache缓存
        codeUserIdCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, Long>() {
            @Override
            public Long load(String s) throws Exception {
                throw new NoVlaInGuavaException("not hit!");
            }
        });

        sessionMap = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Long>() {
            @Override
            public Long load(String userId) {
                throw new NoVlaInGuavaException("not hit!");
            }
        });
    }

    public String genVerifyCode(Long userId) {
        int cnt = 0;
        while (true) {
            String code = CodeGenerateUtil.genCode(cnt++);
            if (codeUserIdCache.getIfPresent(code) != null) {
                continue;
            }

            codeUserIdCache.put(code, userId);
            return code;
        }
    }

    public Long getUserIdByCode(String code) {
        return codeUserIdCache.getIfPresent(code);
    }

    /**
     * 验证码校验成功，生成session并保存
     *
     * @param code
     * @param userId
     * @return
     */
    public String codeVerifySucceed(String code, Long userId) {
        String session = "s-" + UUID.randomUUID();
        sessionMap.put(session, userId);
        // 验证完之后，移除掉，避免重复使用
        codeUserIdCache.invalidate(code);
        return session;
    }

    public void removeSession(String session) {
        sessionMap.invalidate(session);
        sessionMap.cleanUp();
    }

    public Long getUserIdBySession(String session) {
        return sessionMap.getIfPresent(session);
    }
}
