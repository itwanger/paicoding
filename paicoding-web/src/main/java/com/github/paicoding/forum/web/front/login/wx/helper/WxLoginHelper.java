package com.github.paicoding.forum.web.front.login.wx.helper;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.login.LoginQrTypeEnum;
import com.github.paicoding.forum.api.model.exception.NoVlaInGuavaException;
import com.github.paicoding.forum.core.util.CodeGenerateUtil;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class WxLoginHelper {
    /**
     * sse的超时时间，默认15min
     */
    private final static Long SSE_EXPIRE_TIME = 15 * 60 * 1000L;
    private final LoginService sessionService;

    /**
     * key = 验证码, value = 长连接
     */
    private LoadingCache<String, SseEmitter> verifyCodeCache;
    /**
     * key = 设备 value = 验证码
     */
    private LoadingCache<String, String> deviceCodeCache;

    private final WxLoginQrGenIntegration wxLoginQrGenIntegration;

    public WxLoginHelper(LoginService loginService, WxLoginQrGenIntegration wxLoginQrGenIntegration) {
        this.sessionService = loginService;
        this.wxLoginQrGenIntegration = wxLoginQrGenIntegration;
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
                    String code;
                    // 根据登录类型选择不同的验证码生成策略
                    if (wxLoginQrGenIntegration.getLoginQrType() == LoginQrTypeEnum.SERVICE_ACCOUNT) {
                        // 服务号：使用随机验证码，不需要计数器
                        code = CodeGenerateUtil.genCode(cnt, LoginQrTypeEnum.SERVICE_ACCOUNT);
                    } else {
                        // 订阅号：使用specialCodes，需要计数器
                        code = CodeGenerateUtil.genCode(cnt++, LoginQrTypeEnum.SUBSCRIPTION_ACCOUNT);
                    }

                    if (!verifyCodeCache.asMap().containsKey(code)) {
                        return code;
                    }
                }
            }
        });
    }

    /**
     * 保持与前端的长连接
     * <p>
     * 直接根据设备拿之前初始化的验证码，不直接使用传过来的code
     *
     * @return
     */
    public SseEmitter subscribe() throws IOException {
        String deviceId = ReqInfoContext.getReqInfo().getDeviceId();
        String realCode = deviceCodeCache.getUnchecked(deviceId);
        // fixme 设置15min的超时时间, 超时时间一旦设置不能修改；因此导致刷新验证码并不会增加连接的有效期
        SseEmitter sseEmitter = new SseEmitter(SSE_EXPIRE_TIME);
        SseEmitter oldSse = verifyCodeCache.getIfPresent(realCode);
        if (oldSse != null) {
            oldSse.complete();
        }
        verifyCodeCache.put(realCode, sseEmitter);
        sseEmitter.onTimeout(() -> {
            log.info("sse 超时中断 --> {}", realCode);
            verifyCodeCache.invalidate(realCode);
            sseEmitter.complete();
        });
        sseEmitter.onError((e) -> {
            log.warn("sse error! --> {}", realCode, e);
            verifyCodeCache.invalidate(realCode);
            sseEmitter.complete();
        });
        // 若实际的验证码与前端显示的不同，则通知前端更新
        sseEmitter.send("initCode!");
        // 发送用于登录的二维码
        sseEmitter.send("qr#" + wxLoginQrGenIntegration.genLoginQrImg(realCode));
        sseEmitter.send("init#" + realCode);
        return sseEmitter;
    }

    public String resend() throws IOException {
        // 获取旧的验证码，注意不使用 getUnchecked, 避免重新生成一个验证码
        String deviceId = ReqInfoContext.getReqInfo().getDeviceId();
        String oldCode = deviceCodeCache.getIfPresent(deviceId);
        SseEmitter lastSse = oldCode == null ? null : verifyCodeCache.getIfPresent(oldCode);
        if (lastSse != null) {
            lastSse.send("resend!");
            lastSse.send("init#" + oldCode);
            return oldCode;
        }
        return "fail";
    }

    /**
     * 刷新验证码
     *
     * @return
     * @throws IOException
     */
    public String refreshCode() throws IOException {
        String deviceId = ReqInfoContext.getReqInfo().getDeviceId();
        // 获取旧的验证码，注意不使用 getUnchecked, 避免重新生成一个验证码
        String oldCode = deviceCodeCache.getIfPresent(deviceId);
        SseEmitter lastSse = oldCode == null ? null : verifyCodeCache.getIfPresent(oldCode);
        if (lastSse == null) {
            log.info("last deviceId:{}, code:{}, sse closed!", deviceId, oldCode);
            deviceCodeCache.invalidate(deviceId);
            return null;
        }

        // 根据登录类型决定刷新逻辑
        if (wxLoginQrGenIntegration.getLoginQrType() == LoginQrTypeEnum.SERVICE_ACCOUNT) {
            // 服务号登录：刷新二维码图片，不更换验证码
            lastSse.send("refreshQr!");
            String newQrImg = wxLoginQrGenIntegration.genLoginQrImg(oldCode);
            lastSse.send("qr#" + newQrImg);
            log.info("refresh qr image for service account! deviceId:{}, code:{}", deviceId, oldCode);
            return oldCode;
        } else {
            // 普通公众号登录：重新生成验证码
            deviceCodeCache.invalidate(deviceId);
            String newCode = deviceCodeCache.getUnchecked(deviceId);
            log.info("generate new loginCode! deviceId:{}, oldCode:{}, code:{}", deviceId, oldCode, newCode);

            lastSse.send("updateCode!");
            lastSse.send("refresh#" + newCode);
            verifyCodeCache.invalidate(oldCode);
            verifyCodeCache.put(newCode, lastSse);
            return newCode;
        }
    }

    /**
     * 微信公众号登录
     *
     * @param verifyCode 用户输入的登录验证码
     * @return
     */
    public boolean login(String verifyCode) {
        // 1. 通过验证码找到对应的长连接
        SseEmitter sseEmitter = verifyCodeCache.getIfPresent(verifyCode);
        if (sseEmitter == null) {
            return false;
        }

        // 2. 生成登录凭证
        String session = sessionService.loginByWx(ReqInfoContext.getReqInfo().getUserId());
        try {
            // 3. 将登录凭证发送给客户端，用于前端写入Cookie
            // 登录成功，写入session
            sseEmitter.send(session);
            // 设置cookie的路径
            Cookie cookie = SessionUtil.newCookie(LoginService.SESSION_KEY, session);
            String setCookieStr = SessionUtil.buildSetCookieString(cookie);
            sseEmitter.send("login#" + setCookieStr);
            return true;
        } catch (Exception e) {
            log.error("登录异常: {}", verifyCode, e);
        } finally {
            // 4. 登录完成，关闭SSE连接；清空验证码与SseEmitter的绑定关系
            sseEmitter.complete();
            verifyCodeCache.invalidate(verifyCode);
        }
        return false;
    }
}
