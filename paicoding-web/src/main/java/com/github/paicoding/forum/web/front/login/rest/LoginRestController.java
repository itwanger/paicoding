package com.github.paicoding.forum.web.front.login.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.SessionService;
import com.github.paicoding.forum.web.front.login.QrLoginHelper;
import com.github.paicoding.forum.web.front.login.vo.QrLoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 登录/登出的入口
 *
 * @author YiHui
 * @date 2022/8/15
 */
@RestController
@RequestMapping
public class LoginRestController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private QrLoginHelper qrLoginHelper;

    /**
     * 适用于输入验证码的登录流程；
     * 现在使用公众号回调方式登录, 不会走到这个接口
     *
     * @param code
     * @param response
     * @return
     */
    @Deprecated
    @PostMapping("/login")
    public ResVo<Boolean> login(@RequestParam(name = "code") String code,
                                HttpServletResponse response) {
        String session = sessionService.login(code);
        if (StringUtils.isNotBlank(session)) {
            // cookie中写入用户登录信息，用于身份识别
            response.addCookie(SessionUtil.newCookie(SessionService.SESSION_KEY, session));
            return ResVo.ok(true);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录码异常，请重新输入");
        }
    }

    @Permission(role = UserRole.LOGIN)
    @RequestMapping("logout")
    public ResVo<Boolean> logOut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 释放会话
        request.getSession().invalidate();
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> sessionService.logout(s.getSession()));
        // 移除cookie
        response.addCookie(SessionUtil.delCookie(SessionService.SESSION_KEY));
        // 重定向到首页
        response.sendRedirect("/");
        return ResVo.ok(true);
    }

    /**
     * 获取登录的验证码
     *
     * @return
     */
    @GetMapping(path = "/login/code")
    public ResVo<QrLoginVo> qrLogin(HttpServletRequest request, HttpServletResponse response) {
        QrLoginVo vo = new QrLoginVo();
        vo.setCode(qrLoginHelper.genVerifyCode(request, response));
        return ResVo.ok(vo);
    }


    /**
     * 刷新验证码
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping(path = "/login/refresh")
    public ResVo<QrLoginVo> refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        QrLoginVo vo = new QrLoginVo();
        String code = qrLoginHelper.refreshCode(request, response);
        if (StringUtils.isBlank(code)) {
            // 刷新失败，之前的连接已失效，重新建立连接
            code = qrLoginHelper.genVerifyCode(request, response);
            vo.setCode(code);
            vo.setReconnect(true);
        } else {
            vo.setCode(code);
            vo.setReconnect(false);
        }
        return ResVo.ok(vo);
    }
}
