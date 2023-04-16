package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.SessionService;
import com.github.paicoding.forum.service.user.service.UserService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 文章后台
 *
 * @author YiHui
 * @date 2022/12/5
 */
@RestController
@Api(value = "后台登录登出管理控制器", tags = "后台登录")
@RequestMapping(path = {"/api/admin/login", "/admin/login"})
public class AdminLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @PostMapping(path = {"", "/"})
    public ResVo<BaseUserInfoDTO> login(HttpServletRequest request,
                                        HttpServletResponse response) {
        String user = request.getParameter("username");
        String pwd = request.getParameter("password");
        BaseUserInfoDTO info = userService.passwordLogin(user, pwd);
        String session = sessionService.login(info.getUserId());
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息
            response.addCookie(new Cookie(SessionService.SESSION_KEY, session));
            return ResVo.ok(info);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录失败，请重试");
        }
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping("logout")
    public ResVo<Boolean> logOut(HttpServletResponse response) throws IOException {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> sessionService.logout(s.getSession()));
        // 重定向到首页
        response.sendRedirect("/");
        return ResVo.ok(true);
    }
}
