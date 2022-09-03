package com.github.liuyueyi.forum.web.hook.interceptor;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 注入全局的配置信息：
 * - thymleaf 站点信息，基本信息，在这里注入
 *
 * @author yihui
 * @date 2022/6/15
 */
@Slf4j
@Component
public class GlobalViewInterceptor implements AsyncHandlerInterceptor {
    @Autowired
    private GlobalInitService globalInitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Permission permission = handlerMethod.getMethod().getAnnotation(Permission.class);
            if (permission == null) {
                permission = handlerMethod.getBeanType().getAnnotation(Permission.class);
            }

            if (permission == null || permission.role() == UserRole.ALL) {
                return true;
            }
            if (ReqInfoContext.getReqInfo() == null || ReqInfoContext.getReqInfo().getUserId() == null) {
                // 跳转到登录界面
                response.sendRedirect("/login");
                return false;
            }

            if (permission.role() == UserRole.ADMIN && !"admin".equalsIgnoreCase(ReqInfoContext.getReqInfo().getUser().getRole())) {
                // 设置为无权限
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        // 重定向请求不需要添加
//        if (!ObjectUtils.isEmpty(modelAndView)) {
//            modelAndView.getModel().put("env", env);
//            modelAndView.getModel().put("siteInfo", globalViewConfig);
//            if (ReqInfoContext.getReqInfo() == null || ReqInfoContext.getReqInfo().getUserId() == null) {
//                modelAndView.getModel().put("isLogin", false);
//
//            } else {
//                modelAndView.getModel().put("isLogin", true);
//                modelAndView.getModel().put("user", userService.queryUserInfoWithStatistic(ReqInfoContext.getReqInfo().getUserId()));
//                // 消息数 fixme 消息信息改由消息模块处理
//                modelAndView.getModel().put("msgs", Arrays.asList(new UserMsg().setMsgId(100L).setMsgType(1).setMsg("模拟通知消息")));
//            }
//        }
        if (response.getStatus() != HttpStatus.OK.value() && modelAndView != null) {
            try {
                ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
                globalInitService.initLoginUser(reqInfo);
                ReqInfoContext.addReqInfo(reqInfo);
                modelAndView.getModel().put("global", globalInitService.globalAttr());
            } finally {
                ReqInfoContext.clear();
            }
        }
    }
}
