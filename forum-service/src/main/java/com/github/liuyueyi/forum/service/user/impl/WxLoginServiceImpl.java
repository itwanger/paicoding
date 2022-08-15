package com.github.liuyueyi.forum.service.user.impl;

import com.github.liueyueyi.forum.api.model.exception.NoVlaInGuavaException;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.core.util.CodeGenerateUtil;
import com.github.liuyueyi.forum.service.user.LoginService;
import com.github.liuyueyi.forum.service.user.UserService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于微信公众号的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
public class WxLoginServiceImpl implements LoginService {
    private LoadingCache<Long, String> verifyCodeCache;
    private LoadingCache<String, Long> codeUserIdCache;
    private LoadingCache<String, Long> sessionMap;
    @Autowired
    private UserService userService;

    /**
     * todo 知识点：bean完成之后的初始化方式，除了 @PostConstruct 之外还有构造方法方式、实现BeanPostProcessor接口方式
     */
    @PostConstruct
    public void init() {
        // 五分钟内，最多只支持300个用户登录；注意当服务多台机器部署时，基于本地缓存会有问题；请改成redis/memcache缓存
        verifyCodeCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<Long, String>() {
                    @Override
                    public String load(Long userId) {
                        String code = CodeGenerateUtil.genCode();
                        codeUserIdCache.put(code, userId);
                        return code;
                    }
                });
        codeUserIdCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(
                new CacheLoader<String, Long>() {
                    @Override
                    public Long load(String s) throws Exception {
                        throw new NoVlaInGuavaException("not hit!");
                    }
                }
        );

        sessionMap = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Long load(String userId) {
                        throw new NoVlaInGuavaException("not hit!");
                    }
                });
    }

    @Override
    public String getVerifyCode(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        userService.registerOrGetUserInfo(req);
        return verifyCodeCache.getUnchecked(req.getUserId());
    }

    @Override
    public String login(String code) {
        Long userId = codeUserIdCache.getIfPresent(code);
        if (userId != null) {
            String session = "s-" + UUID.randomUUID().toString().replaceAll("-", ".");
            sessionMap.put(session, userId);
            return session;
        }
        return null;
    }

    @Override
    public void logout(String session) {
        sessionMap.invalidate(session);
        sessionMap.cleanUp();
    }


    @Override
    public BaseUserInfoDTO getUserBySessionId(String session) {
        if (StringUtils.isBlank(session)) {
            return null;
        }

        try {
            Long userId = sessionMap.get(session);
            return userService.getUserInfoByUserId(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
