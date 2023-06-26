package com.github.paicoding.forum.web.front.login;

import com.github.paicoding.forum.api.model.exception.NoVlaInGuavaException;
import com.github.paicoding.forum.core.util.CodeGenerateUtil;
import com.github.paicoding.forum.service.user.service.LoginOutService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class QrLoginHelper {
    private final LoginOutService sessionService;
    /**
     * key = 验证码, value = 长连接
     */
    private LoadingCache<String, SseEmitter> verifyCodeCache;
    /**
     * key = 设备 value = 验证码
     */
    private LoadingCache<String, String> deviceCodeCache;

    public QrLoginHelper(LoginOutService loginService) {
        this.sessionService = loginService;
        verifyCodeCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, SseEmitter>() {
            @Override
            public SseEmitter load(String s) throws Exception {
                throw new NoVlaInGuavaException("no val: " + s);
            }
        });

        deviceCodeCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) {
                int cnt = 0;
                while (true) {
                    String code = CodeGenerateUtil.genCode(cnt++);
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
        String deviceId = initDeviceId(request, response);
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
     * 刷新验证码
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public String refreshCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String deviceId = initDeviceId(request, response);
        // 获取旧的验证码，注意不使用 getUnchecked, 避免重新生成一个验证码
        String oldCode = deviceCodeCache.getIfPresent(deviceId);
        SseEmitter lastSse = oldCode == null ? null : verifyCodeCache.getIfPresent(oldCode);
        if (lastSse == null) {
            log.info("last deviceId:{}, code:{}, sse closed!", deviceId, oldCode);
            return null;
        }

        // 重新生成一个验证码
        deviceCodeCache.invalidate(deviceId);
        String newCode = deviceCodeCache.getUnchecked(deviceId);
        log.info("generate new loginCode! deviceId:{}, oldCode:{}, code:{}", deviceId, oldCode, newCode);

        lastSse.send("updateCode!");
        lastSse.send("refresh#" + newCode);
        verifyCodeCache.invalidate(oldCode);
        verifyCodeCache.put(newCode, lastSse);
        return newCode;
    }

    /**
     * 保持与前端的长连接
     * <p>
     * 直接根据设备拿之前初始化的验证码，不直接使用传过来的code
     *
     * @param code
     * @return
     */
    public SseEmitter subscribe(String code) throws IOException {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse res = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        String device = initDeviceId(req, res);
        String realCode = deviceCodeCache.getUnchecked(device);

        // fixme 设置15min的超时时间, 超时时间一旦设置不能修改；因此导致刷新验证码并不会增加连接的有效期
        SseEmitter sseEmitter = new SseEmitter(15 * 60 * 1000L);
        verifyCodeCache.put(code, sseEmitter);
        sseEmitter.onTimeout(() -> verifyCodeCache.invalidate(realCode));
        sseEmitter.onError((e) -> verifyCodeCache.invalidate(realCode));
        if (!Objects.equals(realCode, code)) {
            // 若实际的验证码与前端显示的不同，则通知前端更新
            sseEmitter.send("initCode!");
            sseEmitter.send("init#" + realCode);
        }
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

    public boolean login(String loginCode, String verifyCode) {
        String session = sessionService.register(verifyCode);
        SseEmitter sseEmitter = verifyCodeCache.getIfPresent(loginCode);
        if (sseEmitter != null) {
            try {
                // 登录成功，写入session
                sseEmitter.send(session);
                // 设置cookie的路径
                sseEmitter.send("login#" + LoginOutService.SESSION_KEY + "=" + session + ";path=/;");
                return true;
            } catch (Exception e) {
                log.error("登录异常: {}, {}", loginCode, verifyCode, e);
            } finally {
                sseEmitter.complete();
                verifyCodeCache.invalidate(loginCode);
            }
        }
        return false;
    }

    /**
     * 初始化设备id
     *
     * @param request
     * @param response
     * @return
     */
    public String initDeviceId(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : request.getCookies()) {
                if (LoginOutService.USER_DEVICE_KEY.equalsIgnoreCase(cookie.getName())) {
                    deviceId = cookie.getValue();
                    break;
                }
            }
        }
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            response.addCookie(new Cookie(LoginOutService.USER_DEVICE_KEY, deviceId));
        }
        return deviceId;
    }
}
