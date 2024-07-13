package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.AuthorWhiteListService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 文章后台
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@Tag(name = "后台登录登出管理控制器", description = "后台登录")
@RequestMapping(path = {"/api/admin", "/admin"})
public class AdminLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginOutService;

    @Autowired
    private AuthorWhiteListService articleWhiteListService;

    /**
     * 后台用户名 & 密码的方式登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(path = {"login"})
    public ResVo<BaseUserInfoDTO> login(HttpServletRequest request,
                                        HttpServletResponse response) {
        String username = request.getParameter("username");
        String pwd = request.getParameter("password");
        String session = loginOutService.loginByUserPwd(username, pwd);
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息
            response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(userService.queryBasicUserInfo(ReqInfoContext.getReqInfo().getUserId()));
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录失败，请重试");
        }
    }

    /**
     * 判断是否有登录
     *
     * @return
     */
    @RequestMapping(path = "isLogined")
    public ResVo<Boolean> isLogined() {
        return ResVo.ok(ReqInfoContext.getReqInfo().getUserId() != null);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("info")
    public ResVo<BaseUserInfoDTO> info() {
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        return ResVo.ok(user);
    }

    /**
     * 登出
     *
     * @param response
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping("logout")
    public ResVo<Boolean> logOut(HttpServletResponse response) {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginOutService.logout(s.getSession()));
        // 为什么不后端实现重定向？ 重定向交给前端执行，避免由于前后端分离，本地开发时端口不一致导致的问题
        // response.sendRedirect("/");

        // 移除cookie
        response.addCookie(SessionUtil.delCookie(LoginService.SESSION_KEY));
        return ResVo.ok(true);
    }
}
