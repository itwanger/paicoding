package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ExceptionSeverity;
import com.github.paicoding.forum.api.model.exception.ForumAdviceException;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.exception.model.ExceptionContext;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.notify.service.ExceptionNotifyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>
 * 处理系统中的各类异常，并在必要时发送邮件通知
 * </p>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private ExceptionNotifyService exceptionNotifyService;

    /**
     * 处理业务异常
     */
    @ExceptionHandler(value = ForumAdviceException.class)
    public ResVo<String> handleForumAdviceException(ForumAdviceException e) {
        return ResVo.fail(e.getStatus());
    }

    /**
     * 处理数据库相关异常（严重）
     * <p>
     * 包括SQL异常、数据访问异常等，这些通常意味着数据库连接或查询出现问题
     * </p>
     */
    @ExceptionHandler(value = {SQLException.class, DataAccessException.class})
    public ResVo<String> handleDatabaseException(Exception e) {
        log.error("数据库异常", e);

        // 构建异常上下文并发送邮件通知
        ExceptionContext context = buildExceptionContext(e, ExceptionSeverity.CRITICAL,
            "数据库连接或查询异常，可能影响核心业务功能");

        // 检查是否需要发送通知（带限流）
        if (exceptionNotifyService.shouldNotify(context, true)) {
            exceptionNotifyService.sendExceptionNotify(context);
        }

        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }

    /**
     * 处理IO异常（高优先级）
     * <p>
     * 包括文件操作、网络IO等异常，可能是OSS、文件存储等外部服务问题
     * </p>
     */
    @ExceptionHandler(value = IOException.class)
    public ResVo<String> handleIOException(IOException e) {
        log.error("IO异常", e);

        // 判断异常消息中是否包含特定关键词
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        ExceptionSeverity severity = ExceptionSeverity.HIGH;
        String description = "IO操作异常";

        if (message.contains("oss") || message.contains("aliyun")) {
            description = "OSS服务异常，可能是服务到期或配置错误";
            severity = ExceptionSeverity.HIGH;
        } else if (message.contains("connection") || message.contains("timeout")) {
            description = "网络连接异常，可能是外部服务不可用";
            severity = ExceptionSeverity.HIGH;
        }

        ExceptionContext context = buildExceptionContext(e, severity, description);

        if (exceptionNotifyService.shouldNotify(context, true)) {
            exceptionNotifyService.sendExceptionNotify(context);
        }

        return ResVo.fail(StatusEnum.UPLOAD_PIC_FAILED);
    }

    /**
     * 处理空指针异常（中等优先级）
     * <p>
     * 通常是代码bug，需要关注但不一定影响全局
     * </p>
     */
    @ExceptionHandler(value = NullPointerException.class)
    public ResVo<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);

        ExceptionContext context = buildExceptionContext(e, ExceptionSeverity.MEDIUM,
            "代码空指针异常，可能是某个功能存在bug");

        if (exceptionNotifyService.shouldNotify(context, true)) {
            exceptionNotifyService.sendExceptionNotify(context);
        }

        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }

    /**
     * 处理非法参数异常（低优先级）
     * <p>
     * 通常是用户输入问题，不需要发送邮件
     * </p>
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResVo<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
    }

    /**
     * 处理其他未捕获的异常（高优先级）
     * <p>
     * 兜底处理，捕获所有未预料到的异常
     * </p>
     */
    @ExceptionHandler(value = Exception.class)
    public ResVo<String> handleException(Exception e) {
        log.error("系统异常", e);

        // 判断异常类型，设置不同的严重级别
        ExceptionSeverity severity = ExceptionSeverity.HIGH;
        String description = "系统未捕获异常";

        String exceptionType = e.getClass().getName().toLowerCase();
        if (exceptionType.contains("redis") || exceptionType.contains("cache")) {
            description = "Redis/缓存服务异常";
            severity = ExceptionSeverity.HIGH;
        } else if (exceptionType.contains("rabbitmq") || exceptionType.contains("amqp")) {
            description = "RabbitMQ消息队列异常";
            severity = ExceptionSeverity.HIGH;
        } else if (exceptionType.contains("timeout")) {
            description = "服务超时异常";
            severity = ExceptionSeverity.MEDIUM;
        }

        ExceptionContext context = buildExceptionContext(e, severity, description);

        if (exceptionNotifyService.shouldNotify(context, true)) {
            exceptionNotifyService.sendExceptionNotify(context);
        }

        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }

    /**
     * 构建异常上下文
     *
     * @param exception   异常对象
     * @param severity    严重级别
     * @param description 描述
     * @return 异常上下文
     */
    private ExceptionContext buildExceptionContext(Exception exception, ExceptionSeverity severity,
                                                   String description) {
        ExceptionContext context = ExceptionContext.builder()
            .severity(severity)
            .exceptionType(exception.getClass().getName())
            .exceptionMessage(exception.getMessage())
            .stackTrace(getStackTrace(exception))
            .occurTime(LocalDateTime.now())
            .description(description)
            .methodSignature("GlobalExceptionHandler")
            .className(this.getClass().getName())
            .methodName("handleException")
            .build();

        // 填充请求信息
        fillRequestInfo(context);

        // 填充系统信息
        fillSystemInfo(context);

        return context;
    }

    /**
     * 获取异常堆栈
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 填充请求信息
     */
    private void fillRequestInfo(ExceptionContext context) {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                context.setRequestUrl(request.getRequestURL().toString());
                context.setHttpMethod(request.getMethod());
                context.setRequestParams(request.getQueryString());
                context.setClientIp(getClientIp(request));
                context.setUserAgent(request.getHeader("User-Agent"));

                // 获取TraceId和用户信息
                String traceId = org.slf4j.MDC.get("traceId");
                if (traceId == null) {
                    try {
                        ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getReqInfo();
                        if (reqInfo != null) {
                            traceId = reqInfo.getTraceId();
                            if (reqInfo.getUserId() != null) {
                                context.setUserId(reqInfo.getUserId());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("无法获取ReqInfo", e);
                    }
                }
                context.setTraceId(traceId);
            }
        } catch (Exception e) {
            log.debug("获取请求信息失败", e);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 填充系统信息
     */
    private void fillSystemInfo(ExceptionContext context) {
        try {
            String env = SpringUtil.getConfig("spring.profiles.active", "unknown");
            context.setEnvironment(env);

            String appName = SpringUtil.getConfig("spring.application.name", "paicoding");
            context.setApplicationName(appName);

            try {
                String serverName = InetAddress.getLocalHost().getHostName();
                context.setServerName(serverName);
            } catch (Exception e) {
                context.setServerName("unknown");
            }

            Runtime runtime = Runtime.getRuntime();
            context.setJvmMaxMemory(runtime.maxMemory() / 1024 / 1024);
            context.setJvmTotalMemory(runtime.totalMemory() / 1024 / 1024);
            context.setJvmFreeMemory(runtime.freeMemory() / 1024 / 1024);
            context.setJvmUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);

            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            context.setThreadCount(threadMXBean.getThreadCount());

        } catch (Exception e) {
            log.debug("获取系统信息失败", e);
        }
    }
}
