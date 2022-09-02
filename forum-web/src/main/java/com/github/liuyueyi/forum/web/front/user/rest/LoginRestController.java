package com.github.liuyueyi.forum.web.front.user.rest;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.user.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 登录/登出的入口
 *
 * @author YiHui
 * @date 2022/8/15
 */
@RestController
@RequestMapping(path = "user/api")
public class LoginRestController {
    @Autowired
    private LoginService loginService;

    @RequestMapping("login")
    public ResVo<Boolean> login(@RequestParam(name = "code") String code, HttpServletResponse response) {
        String session = loginService.login(code);
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息
            response.addCookie(new Cookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(true);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录码异常，请重新输入");
        }
    }

    @Permission(role = UserRole.LOGIN)
    @RequestMapping("logout")
    public ResVo<Boolean> logOut() {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginService.logout(s.getSession()));
        return ResVo.ok(true);
    }
}
