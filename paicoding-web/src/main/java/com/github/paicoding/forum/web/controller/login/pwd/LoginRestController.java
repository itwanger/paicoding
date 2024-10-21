package com.github.paicoding.forum.web.controller.login.pwd;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.login.UserNamePasswordReq;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.controller.home.vo.LoginSuccessVo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

/**
 * 用户名 密码方式的登录/登出的入口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@RequestMapping
public class LoginRestController {
    @Autowired
    private LoginService loginService;

    /**
     * 用户名和密码登录
     * 可以根据星球编号/用户名进行密码匹配
     */
    @PostMapping("/login/username")
    public ResVo<Boolean> login(@RequestParam(name = "username") String username,
                                @RequestParam(name = "password") String password,
                                HttpServletResponse response) {
        String session = loginService.loginByUserPwd(username, password);
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息，用于身份识别
            response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(true);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "用户名和密码登录异常，请稍后重试");
        }
    }

    /**
     * 用户名和密码登录
     */
    @PostMapping("/new/login/username")
    public ResVo<LoginSuccessVo> loginByPassword(@RequestBody UserNamePasswordReq req,
                                                 HttpServletResponse response) {
        String session = loginService.loginByUserPwd(req.getUsername(), req.getPassword());
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息，用于身份识别
            Cookie cookie = SessionUtil.newCookie(LoginService.SESSION_KEY, session);
            response.addCookie(cookie);
            return ResVo.ok(new LoginSuccessVo(cookie.getValue()));
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "用户名和密码登录异常，请稍后重试");
        }
    }

    /**
     * 绑定星球账号
     */
    @PostMapping("/login/register")
    public ResVo<Boolean> register(UserPwdLoginReq loginReq,
                                   HttpServletResponse response) {
        String session = loginService.registerByUserPwd(loginReq);
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息，用于身份识别
            response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(true);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "用户名和密码登录异常，请稍后重试");
        }
    }


    @Permission(role = UserRole.LOGIN)
    @RequestMapping("/logout")
    public ResVo<Boolean> logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 释放会话
//        request.getSession().invalidate();
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginService.logout(s.getSession()));
        // 移除cookie
        response.addCookie(SessionUtil.delCookie(LoginService.SESSION_KEY));
        // 重定向到当前页面
//        String referer = request.getHeader("Referer");
//        if (StringUtils.isBlank(referer)) {
//            referer = "/";
//        }
//        response.sendRedirect(referer);
        return ResVo.ok(true);
    }
}
