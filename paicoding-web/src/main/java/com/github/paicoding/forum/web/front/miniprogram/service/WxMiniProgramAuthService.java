package com.github.paicoding.forum.web.front.miniprogram.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginRes;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniProfileReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniUserDTO;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import com.github.paicoding.forum.web.front.miniprogram.config.WxMiniProgramProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 微信小程序登录与资料维护。
 */
@Slf4j
@Service
public class WxMiniProgramAuthService {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";
    private static final String WX_CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
    private static final String THIRD_ACCOUNT_PREFIX = "wxmini:";
    private static final int MAX_NICK_NAME_LENGTH = 50;
    private static final int MAX_AVATAR_URL_LENGTH = 512;
    private static final int MAX_PROFILE_LENGTH = 225;
    private static final int MAX_LOGIN_ATTEMPT_PER_MINUTE = 20;
    private static final String RANDOM_AVATAR_PREFIX = "https://cdn.tobebetterjavaer.com/paicoding/avatar/";

    private final WxMiniProgramProperties properties;
    private final com.github.paicoding.forum.service.user.service.LoginService loginService;
    private final com.github.paicoding.forum.service.user.service.UserService userService;
    private final com.github.paicoding.forum.service.image.service.ImageService imageService;
    private final ImageProperties imageProperties;
    private final Cache<String, AtomicInteger> loginAttemptCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Value("${env.name:dev}")
    private String envName;

    public WxMiniProgramAuthService(WxMiniProgramProperties properties,
                                    com.github.paicoding.forum.service.user.service.LoginService loginService,
                                    com.github.paicoding.forum.service.user.service.UserService userService,
                                    com.github.paicoding.forum.service.image.service.ImageService imageService,
                                    ImageProperties imageProperties) {
        this.properties = properties;
        this.loginService = loginService;
        this.userService = userService;
        this.imageService = imageService;
        this.imageProperties = imageProperties;
    }

    @PostConstruct
    public void validateProductionConfig() {
        if (!isProtectedEnv()) {
            return;
        }
        if (Boolean.TRUE.equals(properties.getMockEnabled())) {
            throw new IllegalStateException("pre/prod 环境禁止启用微信小程序 mock 登录");
        }
        if (StringUtils.isAnyBlank(properties.getAppId(), properties.getAppSecret())) {
            throw new IllegalStateException("pre/prod 环境必须配置微信小程序 AppID/AppSecret");
        }
    }

    public WxMiniLoginRes login(WxMiniLoginReq req) {
        if (req == null || StringUtils.isBlank(req.getCode())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "code不能为空");
        }
        checkLoginRateLimit();

        WxMiniCode2SessionRes sessionRes = code2Session(req.getCode());
        if (StringUtils.isBlank(sessionRes.getOpenId())) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_FAILED_MIXED, "微信登录凭证无效");
        }

        String thirdAccountId = THIRD_ACCOUNT_PREFIX + sessionRes.getOpenId();
        Long userId = loginService.autoRegisterWxUserInfo(thirdAccountId);
        updateProfileIfPresent(userId, req.getNickName(), null);

        UserSessionHelper.SessionDeviceMeta meta = buildSessionMeta();
        String token = loginService.loginByWx(userId, meta);
        BaseUserInfoDTO user = userService.queryBasicUserInfo(userId);
        return new WxMiniLoginRes()
                .setToken(token)
                .setTokenType(TOKEN_TYPE)
                .setTokenHeader(TOKEN_HEADER)
                .setAuthorizationValue(TOKEN_TYPE + " " + token)
                .setUser(toMiniUser(user))
                .setNeedProfile(needProfile(user));
    }

    public WxMiniUserDTO currentUser() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return toMiniUser(userService.queryBasicUserInfo(userId));
    }

    public void logout() {
        String session = ReqInfoContext.getReqInfo().getSession();
        if (StringUtils.isNotBlank(session)) {
            loginService.logout(session);
        }
    }

    public WxMiniUserDTO updateProfile(WxMiniProfileReq req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        if (req != null && req.getNickName() != null && StringUtils.isBlank(req.getNickName())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "昵称不能为空");
        }
        if (req != null && StringUtils.isNotBlank(req.getAvatarUrl())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "头像请通过上传接口提交");
        }
        updateProfileIfPresent(userId, req == null ? null : req.getNickName(), req == null ? null : req.getProfile());
        return toMiniUser(userService.queryBasicUserInfo(userId));
    }

    public WxMiniUserDTO uploadAvatar(HttpServletRequest request) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        String avatarUrl = imageService.saveImg(request);
        saveUploadedAvatar(userId, avatarUrl);
        return toMiniUser(userService.queryBasicUserInfo(userId));
    }

    private WxMiniCode2SessionRes code2Session(String code) {
        if (isProtectedEnv() && Boolean.TRUE.equals(properties.getMockEnabled())) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_FAILED_MIXED, "pre/prod环境禁止启用微信小程序mock登录");
        }
        if (Boolean.TRUE.equals(properties.getMockEnabled())
                && (StringUtils.isAnyBlank(properties.getAppId(), properties.getAppSecret()) || StringUtils.startsWith(code, "mock-"))) {
            return new WxMiniCode2SessionRes().setOpenId("dev_" + Md5Util.encode(code));
        }
        if (StringUtils.isAnyBlank(properties.getAppId(), properties.getAppSecret())) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_FAILED_MIXED, "微信小程序AppID/AppSecret未配置");
        }

        WxMiniCode2SessionRes res = HttpRequestHelper.get(WX_CODE2SESSION_URL,
                MapUtils.create("appid", properties.getAppId(), "secret", properties.getAppSecret(), "code", code),
                WxMiniCode2SessionRes.class);
        if (res == null) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_FAILED_MIXED, "微信code2Session无响应");
        }
        if (StringUtils.isNotBlank(res.getErrCode()) && !"0".equals(res.getErrCode())) {
            log.warn("wx mini code2Session failed, errCode={}, errMsg={}", res.getErrCode(), res.getErrMsg());
            throw ExceptionUtil.of(StatusEnum.LOGIN_FAILED_MIXED, res.getErrMsg());
        }
        return res;
    }

    private void checkLoginRateLimit() {
        String key = buildLoginRateLimitKey();
        AtomicInteger counter = loginAttemptCache.getIfPresent(key);
        if (counter == null) {
            counter = new AtomicInteger();
            loginAttemptCache.put(key, counter);
        }
        if (counter.incrementAndGet() > MAX_LOGIN_ATTEMPT_PER_MINUTE) {
            log.warn("wx mini login rate limited, key={}", key);
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "登录过于频繁，请稍后再试");
        }
    }

    private String buildLoginRateLimitKey() {
        ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getReqInfo();
        if (reqInfo == null) {
            return "unknown";
        }
        String ip = StringUtils.defaultIfBlank(reqInfo.getClientIp(), "unknown");
        String deviceId = StringUtils.defaultIfBlank(reqInfo.getDeviceId(), "unknown");
        return ip + ":" + deviceId;
    }

    private void updateProfileIfPresent(Long userId, String nickName, String profile) {
        if (StringUtils.isBlank(nickName) && profile == null) {
            return;
        }
        UserInfoSaveReq saveReq = new UserInfoSaveReq();
        saveReq.setUserId(userId);
        if (StringUtils.isNotBlank(nickName)) {
            saveReq.setUserName(validateText(nickName, MAX_NICK_NAME_LENGTH, "昵称"));
        }
        if (profile != null) {
            saveReq.setProfile(validateProfileText(profile));
        }
        userService.saveUserInfo(saveReq);
    }

    private void saveUploadedAvatar(Long userId, String avatarUrl) {
        String value = validateUploadedAvatarUrl(avatarUrl);
        UserInfoSaveReq saveReq = new UserInfoSaveReq();
        saveReq.setUserId(userId);
        saveReq.setPhoto(value);
        userService.saveUserInfo(saveReq);
    }

    private String validateText(String text, int maxLength, String fieldName) {
        String value = StringUtils.trim(text);
        if (StringUtils.isBlank(value)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, fieldName + "不能为空");
        }
        if (StringUtils.length(value) > maxLength || StringUtils.containsAny(value, '\n', '\r', '\t')) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, fieldName + "格式不合法");
        }
        return value;
    }

    private String validateProfileText(String profile) {
        String value = StringUtils.trim(profile);
        if (StringUtils.length(value) > MAX_PROFILE_LENGTH || StringUtils.containsAny(value, '\n', '\r', '\t')) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "简介格式不合法");
        }
        return value;
    }

    private String validateUploadedAvatarUrl(String avatarUrl) {
        String value = StringUtils.trim(avatarUrl);
        if (StringUtils.isBlank(value)
                || StringUtils.length(value) > MAX_AVATAR_URL_LENGTH
                || StringUtils.startsWithAny(value, "wxfile://", "http://tmp/", "https://tmp/")) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "头像上传失败");
        }
        if (StringUtils.startsWithAny(value, "http://", "https://") && !isTrustedUploadedAvatarHost(value)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "头像上传失败");
        }
        return value;
    }

    private boolean isTrustedUploadedAvatarHost(String avatarUrl) {
        if (imageProperties == null) {
            return false;
        }
        if (matchesTrustedBaseUrl(avatarUrl, imageProperties.getCdnHost())) {
            return true;
        }
        return imageProperties.getOss() != null
                && matchesTrustedBaseUrl(avatarUrl, imageProperties.getOss().getHost());
    }

    private boolean matchesTrustedBaseUrl(String avatarUrl, String trustedBaseUrl) {
        if (StringUtils.isAnyBlank(avatarUrl, trustedBaseUrl)) {
            return false;
        }
        try {
            URI avatar = URI.create(avatarUrl);
            URI trusted = URI.create(trustedBaseUrl);
            if (StringUtils.isBlank(avatar.getHost()) || StringUtils.isBlank(trusted.getHost())) {
                return false;
            }
            if (!StringUtils.equalsIgnoreCase(avatar.getScheme(), trusted.getScheme())
                    || !StringUtils.equalsIgnoreCase(avatar.getHost(), trusted.getHost())
                    || effectivePort(avatar) != effectivePort(trusted)) {
                return false;
            }
            String trustedPath = StringUtils.defaultIfBlank(trusted.getPath(), "/");
            if ("/".equals(trustedPath)) {
                return true;
            }
            String avatarPath = StringUtils.defaultString(avatar.getPath());
            String trustedPathPrefix = StringUtils.endsWith(trustedPath, "/") ? trustedPath : trustedPath + "/";
            return StringUtils.equals(avatarPath, trustedPath) || StringUtils.startsWith(avatarPath, trustedPathPrefix);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        if (StringUtils.equalsIgnoreCase(uri.getScheme(), "https")) {
            return 443;
        }
        if (StringUtils.equalsIgnoreCase(uri.getScheme(), "http")) {
            return 80;
        }
        return -1;
    }

    private UserSessionHelper.SessionDeviceMeta buildSessionMeta() {
        ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getReqInfo();
        UserSessionHelper.SessionDeviceMeta meta = new UserSessionHelper.SessionDeviceMeta();
        if (reqInfo == null) {
            return meta;
        }
        meta.setDeviceId(reqInfo.getDeviceId());
        meta.setUserAgent(reqInfo.getUserAgent());
        meta.setIp(reqInfo.getClientIp());
        return meta;
    }

    private boolean isProtectedEnv() {
        return StringUtils.equalsAnyIgnoreCase(envName, "pre", "prod");
    }

    private boolean needProfile(BaseUserInfoDTO user) {
        if (user == null) {
            return true;
        }
        String name = user.getUserName();
        return StringUtils.isBlank(name)
                || StringUtils.startsWith(name, "用户")
                || StringUtils.startsWith(name, "user_")
                || StringUtils.isBlank(user.getPhoto())
                || isDefaultRandomAvatar(user.getPhoto());
    }

    private boolean isDefaultRandomAvatar(String photo) {
        String value = StringUtils.trim(photo);
        if (!StringUtils.startsWith(value, RANDOM_AVATAR_PREFIX) || !StringUtils.endsWithIgnoreCase(value, ".png")) {
            return false;
        }
        String fileName = StringUtils.substringAfterLast(value, "/");
        String number = StringUtils.substringBeforeLast(fileName, ".");
        if (StringUtils.length(number) != 4) {
            return false;
        }
        for (int i = 0; i < number.length(); i++) {
            if (!Character.isDigit(number.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private WxMiniUserDTO toMiniUser(BaseUserInfoDTO user) {
        if (user == null) {
            return null;
        }
        return new WxMiniUserDTO()
                .setUserId(user.getUserId())
                .setNickName(user.getUserName())
                .setAvatarUrl(user.getPhoto())
                .setRole(user.getRole())
                .setProfile(user.getProfile());
    }

    @Data
    private static class WxMiniCode2SessionRes {
        @JsonProperty("openid")
        private String openId;
        @JsonProperty("session_key")
        private String sessionKey;
        @JsonProperty("unionid")
        private String unionId;
        @JsonProperty("errcode")
        private String errCode;
        @JsonProperty("errmsg")
        private String errMsg;

        public WxMiniCode2SessionRes setOpenId(String openId) {
            this.openId = openId;
            return this;
        }
    }
}
