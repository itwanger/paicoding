package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常处理
 * fixme: 除了这种姿势之外，还可以使用 ControllerAdvice 注解方式
 *
 * @author YiHui
 * @date 2022/9/3
 */
@Slf4j
@Order(-100)
public class ForumExceptionHandler implements HandlerExceptionResolver {

    /**
     * 公开接口的兜底文案。异常细节（堆栈、框架类名、SQL 片段）只写日志：
     * /search/api/** 这类匿名可访问的接口曾把完整 Java 堆栈打进响应体，属于信息泄露。
     */
    private static final String DEFAULT_ERROR_MSG = "服务异常，请稍后重试";

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Status errStatus = buildToastMsg(ex, isAdminRequest(request));

        if (restResponse(request, response)) {
            // 表示返回json数据格式的异常提示信息
            if (response.isCommitted()) {
                // 如果返回已经提交过，直接退出即可
                return new ModelAndView();
            }

            try {
                response.reset();
                // 若是rest接口请求异常时，返回json格式的异常数据；而不是专门的500页面
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.setHeader("Cache-Control", "no-cache, must-revalidate");
                response.getWriter().println(JsonUtil.toStr(ResVo.fail(errStatus)));
                response.getWriter().flush();
                response.getWriter().close();
                return new ModelAndView();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String view = getErrorPage(errStatus, response);
        ModelAndView mv = new ModelAndView(view);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        mv.getModel().put("global", SpringUtil.getBean(GlobalInitService.class).globalAttr());
        mv.getModel().put("res", ResVo.fail(errStatus));
        mv.getModel().put("toast", JsonUtil.toStr(ResVo.fail(errStatus)));
        return mv;
    }

    /**
     * @param detailed 是否回传异常细节。仅后台请求为 true——后台路径由 {@code @Permission}
     *                 拦截器挡在登录态之后，细节有助于排查；公开接口一律只回通用文案
     */
    private Status buildToastMsg(Exception ex, boolean detailed) {
        if (ex instanceof ForumException) {
            // 业务异常的 msg 是特意写给用户看的，保留
            return ((ForumException) ex).getStatus();
        } else if (ex instanceof AsyncRequestTimeoutException) {
            return Status.newStatus(StatusEnum.UNEXPECT_ERROR, "超时未登录");
        } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
            log.warn("media type not acceptable! {}", ReqInfoContext.getReqInfo(), ex);
            return Status.newStatus(StatusEnum.RECORDS_NOT_EXISTS, detailed ? detailOf(ex) : "请求的资源");
        } else if (ex instanceof HttpRequestMethodNotSupportedException
                || ex instanceof MethodArgumentTypeMismatchException
                || ex instanceof ServletRequestBindingException
                || ex instanceof IOException) {
            // 请求方法/参数不匹配，是调用方的问题，记 warn 即可
            log.warn("illegal request! {}", ReqInfoContext.getReqInfo(), ex);
            return detailed
                    ? Status.newStatus(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, detailOf(ex))
                    : Status.newStatus(StatusEnum.ILLEGAL_ARGUMENTS);
        } else {
            if (ex instanceof NestedRuntimeException) {
                log.error("unexpect NestedRuntimeException error! {}", ReqInfoContext.getReqInfo(), ex);
            } else {
                log.error("unexpect error! {}", ReqInfoContext.getReqInfo(), ex);
            }
            return detailed
                    ? Status.newStatus(StatusEnum.UNEXPECT_ERROR, detailOf(ex))
                    : Status.newStatus(StatusEnum.UNEXPECT_ERROR.getCode(), DEFAULT_ERROR_MSG);
        }
    }

    /**
     * 取根因的「类名: 描述」单行摘要。包装异常的 message 往往是
     * "Request processing failed; nested exception is ..." 这类噪音，根因才有价值；
     * 完整堆栈在日志里，不塞进只能显示一行的 toast。
     */
    private String detailOf(Exception ex) {
        return ExceptionUtils.getRootCauseMessage(ex);
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/admin/") || uri.startsWith("/api/admin/");
    }

    private String getErrorPage(Status status, HttpServletResponse response) {
        // 根据异常码解析需要返回的错误页面
        if (StatusEnum.is5xx(status.getCode())) {
            response.setStatus(500);
            return "error/500";
        } else if (StatusEnum.is403(status.getCode())) {
            response.setStatus(403);
            return "error/403";
        } else {
            response.setStatus(404);
            return "error/404";
        }
    }

    /**
     * 后台请求、api数据请求、上传图片等接口，返回json格式的异常提示信息
     * 其他异常，返回500的页面
     *
     * @param request
     * @param response
     * @return
     */
    private boolean restResponse(HttpServletRequest request, HttpServletResponse response) {
        if (isAdminRequest(request)) {
            return true;
        }

        if (request.getRequestURI().startsWith("/image/upload")) {
            return true;
        }

        if (response.getContentType() != null && response.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        if (isAjaxRequest(request)) {
            return true;
        }

        // 数据接口请求
        AntPathMatcher pathMatcher = new AntPathMatcher();
        if (pathMatcher.match("/**/api/**", request.getRequestURI())) {
            return true;
        }
        return false;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

}
