package com.github.liuyueyi.forum.web.front.login;

import com.github.liueyueyi.forum.api.model.exception.NoVlaInGuavaException;
import com.github.liuyueyi.forum.core.util.CodeGenerateUtil;
import com.github.liuyueyi.forum.service.user.service.LoginService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class QrLoginHelper {
    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx4a128c315d9b1228&secret=077e2d92dee69f04ba6d53a0ef4459f9";
    /**
     * 访问token
     */
    public static volatile String token = "";

    /**
     * 失效时间
     */
    public static volatile long expireTime = 0L;


    private final LoginService loginService;
    /**
     * key = 验证码, value = 长连接
     */
    private LoadingCache<String, SseEmitter> verifyCodeCache;
    /**
     * key = 设备 value = 验证码
     */
    private LoadingCache<String, String> deviceCodeCache;

    public QrLoginHelper(LoginService loginService) {
        this.loginService = loginService;
        verifyCodeCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, SseEmitter>() {
            @Override
            public SseEmitter load(String s) throws Exception {
                throw new NoVlaInGuavaException("no val: " + s);
            }
        });

        deviceCodeCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) {
                while (true) {
                    String code = CodeGenerateUtil.genCode();
                    if (!verifyCodeCache.asMap().containsKey(code)) {
                        return code;
                    }
                }
            }
        });
    }

    /**
     * 加一层设备id，主要目的就是为了避免不断刷新页面时，不断的往 verifyCodeCache 中塞入新的kv对
     * 其次就是确保五分钟内，不管刷新多少次，验证码都一样
     *
     * @param request
     * @param response
     * @return
     */
    public String genVerifyCode(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = null;
        for (Cookie cookie : request.getCookies()) {
            if (LoginService.USER_DEVICE_KEY.equalsIgnoreCase(cookie.getName())) {
                deviceId = cookie.getValue();
                break;
            }
        }
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            response.addCookie(new Cookie(LoginService.USER_DEVICE_KEY, deviceId));
        }

        String code = deviceCodeCache.getUnchecked(deviceId);
        SseEmitter lastSse = verifyCodeCache.getIfPresent(code);
        if (lastSse != null) {
            // 这个设备之前已经建立了连接，则移除旧的，重新再建立一个; 通常是不断刷新登录页面，会出现这个场景
            lastSse.complete();
            verifyCodeCache.invalidate(code);
        }
        return code;
    }

    /**
     * 保持与前端的长连接
     *
     * @param code
     * @return
     */
    public SseEmitter subscribe(String code) {
        SseEmitter sseEmitter = new SseEmitter(5 * 60 * 1000L);
        verifyCodeCache.put(code, sseEmitter);
        sseEmitter.onTimeout(() -> verifyCodeCache.invalidate(code));
        sseEmitter.onError((e) -> verifyCodeCache.invalidate(code));
        return sseEmitter;
    }


    /**
     * 二维码已扫描
     *
     * @param code
     * @throws IOException
     */
    public void scan(String code) throws IOException {
        SseEmitter sseEmitter = verifyCodeCache.getIfPresent(code);
        if (sseEmitter != null) {
            sseEmitter.send("scan");
        }
    }

    public void login(String loginCode, String verifyCode) {
        String session = loginService.login(verifyCode);
        SseEmitter sseEmitter = verifyCodeCache.getIfPresent(loginCode);
        if (sseEmitter != null) {
            try {
                // 登录成功，写入session
                sseEmitter.send(session);
                sseEmitter.send("login#" + LoginService.SESSION_KEY + "=" + session);
            } catch (Exception e) {
                log.error("登录异常: {}, {}", loginCode, verifyCode, e);
            } finally {
                sseEmitter.complete();
                verifyCodeCache.invalidate(loginCode);
            }
        }
    }
}
