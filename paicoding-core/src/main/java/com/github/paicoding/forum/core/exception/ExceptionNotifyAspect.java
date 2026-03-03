package com.github.paicoding.forum.core.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.annotation.ExceptionNotify;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.exception.model.ExceptionContext;
import com.github.paicoding.forum.core.exception.service.ExceptionNotifyService;
import com.github.paicoding.forum.core.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异常通知切面
 * <p>
 * 拦截 @ExceptionNotify 注解标记的方法，当方法抛出异常时，收集异常上下文信息并发送邮件通知
 * </p>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Slf4j
@Aspect
@Component
public class ExceptionNotifyAspect {

    @Resource
    private ExceptionNotifyService exceptionNotifyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异常抛出后通知
     *
     * @param joinPoint 切点
     * @param exception 异常
     * @param notify    注解
     */
    @AfterThrowing(pointcut = "@annotation(notify)", throwing = "exception")
    public void handleException(JoinPoint joinPoint, Throwable exception, ExceptionNotify notify) {
        try {
            log.debug("捕获到异常，准备发送邮件通知: {}", exception.getClass().getName());

            // 构建异常上下文
            ExceptionContext context = buildExceptionContext(joinPoint, exception, notify);

            // 检查是否需要发送通知（考虑限流）
            if (!exceptionNotifyService.shouldNotify(context, notify.enableRateLimit())) {
                log.debug("异常通知被限流，跳过发送: {}", exception.getClass().getName());
                return;
            }

            // 异步发送邮件通知
            exceptionNotifyService.sendExceptionNotify(context, notify.notifyEmails());

        } catch (Exception e) {
            // 异常通知本身不应该影响业务流程，只记录日志
            log.error("发送异常通知时发生错误", e);
        }
    }

    /**
     * 构建异常上下文
     *
     * @param joinPoint 切点
     * @param exception 异常
     * @param notify    注解
     * @return 异常上下文
     */
    private ExceptionContext buildExceptionContext(JoinPoint joinPoint, Throwable exception,
                                                   ExceptionNotify notify) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ExceptionContext context = ExceptionContext.builder()
            // 基础信息
            .severity(notify.severity())
            .exceptionType(exception.getClass().getName())
            .exceptionMessage(exception.getMessage())
            .stackTrace(getStackTrace(exception))
            .occurTime(LocalDateTime.now())
            .description(notify.description())
            // 方法信息
            .methodSignature(signature.toShortString())
            .className(signature.getDeclaringTypeName())
            .methodName(method.getName())
            .build();

        // 方法参数（如果启用）
        if (notify.includeArgs()) {
            context.setMethodArgs(buildMethodArgs(signature, joinPoint.getArgs()));
        }

        // 请求信息（如果在Web环境中）
        fillRequestInfo(context);

        // 系统信息
        fillSystemInfo(context);

        return context;
    }

    /**
     * 获取异常堆栈信息
     *
     * @param throwable 异常
     * @return 堆栈字符串
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 构建方法参数JSON
     *
     * @param signature 方法签名
     * @param args      参数值
     * @return JSON字符串
     */
    private String buildMethodArgs(MethodSignature signature, Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            String[] paramNames = signature.getParameterNames();
            Map<String, Object> argsMap = new HashMap<>();

            for (int i = 0; i < args.length; i++) {
                String paramName = (paramNames != null && i < paramNames.length) ?
                    paramNames[i] : "arg" + i;

                // 对于某些特殊对象（如Request、Response等），只记录类型
                Object arg = args[i];
                if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                    argsMap.put(paramName, arg.getClass().getSimpleName());
                } else {
                    argsMap.put(paramName, arg);
                }
            }

            return objectMapper.writeValueAsString(argsMap);
        } catch (Exception e) {
            log.warn("序列化方法参数失败", e);
            return Arrays.toString(args);
        }
    }

    /**
     * 填充请求信息
     *
     * @param context 异常上下文
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

                // 从MDC或ReqInfoContext获取TraceId
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

                // 请求头（只记录部分重要的）
                Map<String, String> headers = new HashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        // 只记录部分重要的请求头
                        if (headerName.toLowerCase().matches("(content-type|accept|referer|origin)")) {
                            headers.put(headerName, request.getHeader(headerName));
                        }
                    }
                }
                context.setRequestHeaders(headers);
            }
        } catch (Exception e) {
            log.debug("获取请求信息失败", e);
        }
    }

    /**
     * 获取客户端真实IP
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 填充系统信息
     *
     * @param context 异常上下文
     */
    private void fillSystemInfo(ExceptionContext context) {
        try {
            // 环境
            String env = SpringUtil.getConfig("spring.profiles.active", "unknown");
            context.setEnvironment(env);

            // 应用名称
            String appName = SpringUtil.getConfig("spring.application.name", "paicoding");
            context.setApplicationName(appName);

            // 服务器名称
            try {
                String serverName = InetAddress.getLocalHost().getHostName();
                context.setServerName(serverName);
            } catch (Exception e) {
                context.setServerName("unknown");
            }

            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long usedMemory = totalMemory - freeMemory;

            context.setJvmMaxMemory(maxMemory);
            context.setJvmTotalMemory(totalMemory);
            context.setJvmFreeMemory(freeMemory);
            context.setJvmUsedMemory(usedMemory);

            // 线程数
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            context.setThreadCount(threadMXBean.getThreadCount());

        } catch (Exception e) {
            log.debug("获取系统信息失败", e);
        }
    }
}
