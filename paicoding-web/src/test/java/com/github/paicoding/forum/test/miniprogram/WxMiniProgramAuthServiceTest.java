package com.github.paicoding.forum.test.miniprogram;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginRes;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniProfileReq;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.config.OssProperties;
import com.github.paicoding.forum.service.image.service.ImageService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.miniprogram.config.WxMiniProgramProperties;
import com.github.paicoding.forum.web.front.miniprogram.service.WxMiniProgramAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WxMiniProgramAuthServiceTest {

    @AfterEach
    public void tearDown() {
        ReqInfoContext.clear();
    }

    @Test
    public void shouldRejectMockLoginInProd() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        properties.setAppId("appid");
        properties.setAppSecret("secret");

        WxMiniProgramAuthService service = newService(properties, mock(LoginService.class), mock(UserService.class));
        ReflectionTestUtils.setField(service, "envName", "prod");

        assertThrows(IllegalStateException.class, service::validateProductionConfig);
    }

    @Test
    public void shouldRequireAppSecretInPre() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(false);
        properties.setAppId("appid");

        WxMiniProgramAuthService service = newService(properties, mock(LoginService.class), mock(UserService.class));
        ReflectionTestUtils.setField(service, "envName", "pre");

        assertThrows(IllegalStateException.class, service::validateProductionConfig);
    }

    @Test
    public void shouldReturnBearerContractForDevMockLogin() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        LoginService loginService = mock(LoginService.class);
        UserService userService = mock(UserService.class);
        when(loginService.autoRegisterWxUserInfo(any())).thenReturn(1024L);
        when(loginService.loginByWx(eq(1024L), any())).thenReturn("session-token");
        when(userService.queryBasicUserInfo(1024L)).thenReturn(new BaseUserInfoDTO().setUserId(1024L).setUserName("用户1024"));

        WxMiniProgramAuthService service = newService(properties, loginService, userService);
        ReflectionTestUtils.setField(service, "envName", "dev");
        WxMiniLoginReq req = new WxMiniLoginReq();
        req.setCode("mock-unit");
        WxMiniLoginRes res = service.login(req);

        assertEquals("Authorization", res.getTokenHeader());
        assertEquals("Bearer", res.getTokenType());
        assertEquals("Bearer session-token", res.getAuthorizationValue());
        assertTrue(res.getNeedProfile());
    }

    @Test
    public void shouldIgnoreAvatarUrlDuringLoginProfilePatch() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        LoginService loginService = mock(LoginService.class);
        UserService userService = mock(UserService.class);
        when(loginService.autoRegisterWxUserInfo(any())).thenReturn(1024L);
        when(loginService.loginByWx(eq(1024L), any())).thenReturn("session-token");
        when(userService.queryBasicUserInfo(1024L)).thenReturn(new BaseUserInfoDTO().setUserId(1024L).setUserName("二哥"));

        WxMiniProgramAuthService service = newService(properties, loginService, userService);
        ReflectionTestUtils.setField(service, "envName", "dev");
        WxMiniLoginReq req = new WxMiniLoginReq();
        req.setCode("mock-unit");
        req.setNickName(" 二哥 ");
        req.setAvatarUrl("unexpected-avatar-value");
        WxMiniLoginRes res = service.login(req);

        ArgumentCaptor<UserInfoSaveReq> captor = ArgumentCaptor.forClass(UserInfoSaveReq.class);
        verify(userService).saveUserInfo(captor.capture());
        assertEquals(Long.valueOf(1024L), captor.getValue().getUserId());
        assertEquals("二哥", captor.getValue().getUserName());
        assertEquals(null, captor.getValue().getPhoto());
        assertTrue(res.getNeedProfile());
    }

    @Test
    public void shouldReturnNeedProfileFalseWhenServerProfileIsComplete() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        LoginService loginService = mock(LoginService.class);
        UserService userService = mock(UserService.class);
        when(loginService.autoRegisterWxUserInfo(any())).thenReturn(1024L);
        when(loginService.loginByWx(eq(1024L), any())).thenReturn("session-token");
        when(userService.queryBasicUserInfo(1024L)).thenReturn(new BaseUserInfoDTO()
                .setUserId(1024L)
                .setUserName("二哥")
                .setPhoto("https://cdn.paicoding.com/avatar.png"));

        WxMiniProgramAuthService service = newService(properties, loginService, userService);
        ReflectionTestUtils.setField(service, "envName", "dev");
        WxMiniLoginReq req = new WxMiniLoginReq();
        req.setCode("mock-unit");
        WxMiniLoginRes res = service.login(req);

        assertEquals(Boolean.FALSE, res.getNeedProfile());
    }

    @Test
    public void shouldRequireProfileWhenServerOnlyHasRandomDefaultAvatar() {
        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        LoginService loginService = mock(LoginService.class);
        UserService userService = mock(UserService.class);
        when(loginService.autoRegisterWxUserInfo(any())).thenReturn(1024L);
        when(loginService.loginByWx(eq(1024L), any())).thenReturn("session-token");
        when(userService.queryBasicUserInfo(1024L)).thenReturn(new BaseUserInfoDTO()
                .setUserId(1024L)
                .setUserName("快乐的皮卡丘")
                .setPhoto("https://cdn.tobebetterjavaer.com/paicoding/avatar/0007.png"));

        WxMiniProgramAuthService service = newService(properties, loginService, userService);
        ReflectionTestUtils.setField(service, "envName", "dev");
        WxMiniLoginReq req = new WxMiniLoginReq();
        req.setCode("mock-unit");
        WxMiniLoginRes res = service.login(req);

        assertEquals(Boolean.TRUE, res.getNeedProfile());
    }

    @Test
    public void shouldRateLimitFrequentLoginAttempts() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setClientIp("127.0.0.1");
        reqInfo.setDeviceId("device-a");
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProgramProperties properties = new WxMiniProgramProperties();
        properties.setMockEnabled(true);
        LoginService loginService = mock(LoginService.class);
        UserService userService = mock(UserService.class);
        when(loginService.autoRegisterWxUserInfo(any())).thenReturn(1024L);
        when(loginService.loginByWx(eq(1024L), any())).thenReturn("session-token");
        when(userService.queryBasicUserInfo(1024L)).thenReturn(new BaseUserInfoDTO().setUserId(1024L).setUserName("用户1024"));

        WxMiniProgramAuthService service = newService(properties, loginService, userService);
        ReflectionTestUtils.setField(service, "envName", "dev");
        for (int i = 0; i < 20; i++) {
            WxMiniLoginReq req = new WxMiniLoginReq();
            req.setCode("mock-unit-" + i);
            service.login(req);
        }

        WxMiniLoginReq req = new WxMiniLoginReq();
        req.setCode("mock-unit-over-limit");
        assertThrows(ForumException.class, () -> service.login(req));
    }

    @Test
    public void shouldRejectRemoteAvatarUrlInProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setAvatarUrl("https://example.com/avatar.png");
        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class));
        assertThrows(ForumException.class, () -> service.updateProfile(req));
    }

    @Test
    public void shouldRejectWxfileAvatarUrlInProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setAvatarUrl("wxfile://tmp_avatar.png");
        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class));

        assertThrows(ForumException.class, () -> service.updateProfile(req));
    }

    @Test
    public void shouldRejectArbitraryAvatarValueInProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setAvatarUrl("unexpected-avatar-value");
        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class));

        assertThrows(ForumException.class, () -> service.updateProfile(req));
    }

    @Test
    public void shouldRejectBlankNicknameInProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setNickName("   ");
        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class));

        assertThrows(ForumException.class, () -> service.updateProfile(req));
    }

    @Test
    public void shouldRejectTooLongProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setProfile(new String(new char[226]).replace('\0', 'a'));
        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class));

        assertThrows(ForumException.class, () -> service.updateProfile(req));
    }

    @Test
    public void shouldTrimAndSaveValidNickname() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        UserService userService = mock(UserService.class);
        when(userService.queryBasicUserInfo(100L)).thenReturn(new BaseUserInfoDTO().setUserId(100L).setUserName("沉默王二"));
        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setNickName(" 沉默王二 ");

        newService(new WxMiniProgramProperties(), mock(LoginService.class), userService).updateProfile(req);

        ArgumentCaptor<UserInfoSaveReq> captor = ArgumentCaptor.forClass(UserInfoSaveReq.class);
        verify(userService).saveUserInfo(captor.capture());
        assertEquals(Long.valueOf(100L), captor.getValue().getUserId());
        assertEquals("沉默王二", captor.getValue().getUserName());
        assertNotNull(captor.getValue());
    }

    @Test
    public void shouldAllowClearingProfile() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        UserService userService = mock(UserService.class);
        when(userService.queryBasicUserInfo(100L)).thenReturn(new BaseUserInfoDTO()
                .setUserId(100L)
                .setUserName("沉默王二")
                .setProfile(""));
        WxMiniProfileReq req = new WxMiniProfileReq();
        req.setProfile("   ");

        newService(new WxMiniProgramProperties(), mock(LoginService.class), userService).updateProfile(req);

        ArgumentCaptor<UserInfoSaveReq> captor = ArgumentCaptor.forClass(UserInfoSaveReq.class);
        verify(userService).saveUserInfo(captor.capture());
        assertEquals(Long.valueOf(100L), captor.getValue().getUserId());
        assertEquals("", captor.getValue().getProfile());
    }

    @Test
    public void shouldAllowServerUploadedCdnAvatarUrl() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        String avatarUrl = "https://cdn.paicoding.com/paicoding/avatar/"
                + new String(new char[160]).replace('\0', 'a')
                + ".png";
        ImageService imageService = mock(ImageService.class);
        UserService userService = mock(UserService.class);
        when(imageService.saveImg(any(HttpServletRequest.class))).thenReturn(avatarUrl);
        when(userService.queryBasicUserInfo(100L)).thenReturn(new BaseUserInfoDTO()
                .setUserId(100L)
                .setUserName("沉默王二")
                .setPhoto(avatarUrl));

        newService(new WxMiniProgramProperties(), mock(LoginService.class), userService, imageService)
                .uploadAvatar(new MockHttpServletRequest());

        ArgumentCaptor<UserInfoSaveReq> captor = ArgumentCaptor.forClass(UserInfoSaveReq.class);
        verify(userService).saveUserInfo(captor.capture());
        assertEquals(Long.valueOf(100L), captor.getValue().getUserId());
        assertEquals(avatarUrl, captor.getValue().getPhoto());
    }

    @Test
    public void shouldRejectOversizedServerUploadedAvatarUrl() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        String avatarUrl = "https://cdn.paicoding.com/paicoding/avatar/"
                + new String(new char[512]).replace('\0', 'a')
                + ".png";
        ImageService imageService = mock(ImageService.class);
        when(imageService.saveImg(any(HttpServletRequest.class))).thenReturn(avatarUrl);

        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class), imageService);

        assertThrows(ForumException.class, () -> service.uploadAvatar(new MockHttpServletRequest()));
    }

    @Test
    public void shouldRejectThirdPartyServerUploadedAvatarUrl() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        ImageService imageService = mock(ImageService.class);
        when(imageService.saveImg(any(HttpServletRequest.class))).thenReturn("https://evil.example.com/avatar.png");

        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class), imageService);

        assertThrows(ForumException.class, () -> service.uploadAvatar(new MockHttpServletRequest()));
    }

    @Test
    public void shouldRejectLookalikeCdnHostAvatarUrl() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(100L);
        ReqInfoContext.addReqInfo(reqInfo);

        ImageService imageService = mock(ImageService.class);
        when(imageService.saveImg(any(HttpServletRequest.class))).thenReturn("https://cdn.paicoding.com.evil.example/avatar.png");

        WxMiniProgramAuthService service = newService(new WxMiniProgramProperties(), mock(LoginService.class), mock(UserService.class), imageService);

        assertThrows(ForumException.class, () -> service.uploadAvatar(new MockHttpServletRequest()));
    }

    private WxMiniProgramAuthService newService(WxMiniProgramProperties properties,
                                                com.github.paicoding.forum.service.user.service.LoginService loginService,
                                                com.github.paicoding.forum.service.user.service.UserService userService) {
        return newService(properties, loginService, userService, mock(ImageService.class));
    }

    private WxMiniProgramAuthService newService(WxMiniProgramProperties properties,
                                                com.github.paicoding.forum.service.user.service.LoginService loginService,
                                                com.github.paicoding.forum.service.user.service.UserService userService,
                                                ImageService imageService) {
        return new WxMiniProgramAuthService(properties, loginService, userService, imageService, imageProperties());
    }

    private ImageProperties imageProperties() {
        ImageProperties imageProperties = new ImageProperties();
        imageProperties.setCdnHost("https://cdn.paicoding.com/");
        OssProperties oss = new OssProperties();
        oss.setHost("https://cdn.tobebetterjavaer.com/");
        imageProperties.setOss(oss);
        return imageProperties;
    }
}
