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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(path = {"/api/admin", "/admin"})
public class AdminLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    /**
     * 后台用户名 & 密码的方式登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(path = {"/login"})
    public ResVo<BaseUserInfoDTO> login(HttpServletRequest request,
                                        HttpServletResponse response) {
        String user = request.getParameter("username");
        String pwd = request.getParameter("password");
        BaseUserInfoDTO info = userService.passwordLogin(user, pwd);
        String session = sessionService.login(info.getUserId());
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息
            Cookie cookie = new Cookie(SessionService.SESSION_KEY, session);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResVo.ok(info);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录失败，请重试");
        }
    }

    /**
     * 判断是否有登录
     *
     * @param request
     * @return
     */
    @RequestMapping(path = "/isLogined")
    public ResVo<Boolean> isLogined(HttpServletRequest request) {
        return ResVo.ok(ReqInfoContext.getReqInfo().getUserId() != null);
    }

    /**
     * 登出
     *
     * @param response
     * @return
     * @throws IOException
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping("logout")
    public ResVo<Boolean> logOut(HttpServletResponse response) throws IOException {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> sessionService.logout(s.getSession()));
        // 重定向交给前端执行，避免由于前后端分离，本地开发时端口不一致导致的问题
//        response.sendRedirect("/");
        return ResVo.ok(true);
    }
}
