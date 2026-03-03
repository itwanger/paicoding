package com.github.paicoding.forum.service.notify.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.core.exception.EmailRateLimiter;
import com.github.paicoding.forum.core.exception.model.ExceptionContext;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.service.notify.service.ExceptionNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 异常通知服务实现
 * <p>
 * 负责构建和发送异常邮件通知
 * </p>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Slf4j
@Service
public class ExceptionNotifyServiceImpl implements ExceptionNotifyService {

    /**
     * 邮件限流器（10分钟窗口期）
     */
    private final EmailRateLimiter rateLimiter;

    /**
     * 默认通知邮箱
     */
    private final String defaultNotifyEmail;

    /**
     * 是否启用邮件通知
     */
    private final boolean notifyEnabled;

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * ObjectMapper 用于JSON序列化
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExceptionNotifyServiceImpl(
        @Value("${exception.notify.rate-limit.window-minutes:10}") int windowMinutes,
        @Value("${exception.notify.emails:xhhuiblog@163.com}") String defaultNotifyEmail,
        @Value("${exception.notify.enabled:true}") boolean notifyEnabled) {
        this.rateLimiter = new EmailRateLimiter(windowMinutes);
        this.defaultNotifyEmail = defaultNotifyEmail;
        this.notifyEnabled = notifyEnabled;

        log.info("异常通知服务初始化完成 - 启用状态: {}, 默认邮箱: {}, 限流窗口: {}分钟",
            notifyEnabled, defaultNotifyEmail, windowMinutes);
    }

    @Override
    public boolean sendExceptionNotify(ExceptionContext context) {
        return sendExceptionNotify(context, null);
    }

    @Override
    @Async("asyncExecutor")
    public boolean sendExceptionNotify(ExceptionContext context, String notifyEmails) {
        if (!notifyEnabled) {
            log.debug("异常邮件通知功能未启用，跳过发送");
            return false;
        }

        try {
            // 确定接收邮箱
            String targetEmails = StringUtils.hasText(notifyEmails) ?
                notifyEmails : defaultNotifyEmail;

            // 构建邮件标题
            String subject = buildEmailSubject(context);

            // 构建邮件内容
            String content = buildEmailContent(context);

            // 发送邮件（支持多个收件人）
            String[] emailArray = targetEmails.split(",");
            boolean allSuccess = true;
            for (String email : emailArray) {
                email = email.trim();
                if (StringUtils.hasText(email)) {
                    boolean success = EmailUtil.sendMail(subject, email, content);
                    if (!success) {
                        log.error("发送异常通知邮件失败 - 收件人: {}", email);
                        allSuccess = false;
                    } else {
                        log.info("异常通知邮件发送成功 - 收件人: {}, 异常: {}",
                            email, context.getExceptionType());
                    }
                }
            }

            return allSuccess;

        } catch (Exception e) {
            log.error("发送异常通知邮件时发生错误", e);
            return false;
        }
    }

    @Override
    public boolean shouldNotify(ExceptionContext context, boolean enableRateLimit) {
        if (!notifyEnabled) {
            return false;
        }

        if (!enableRateLimit) {
            return true;
        }

        // 使用限流器判断
        boolean allowed = rateLimiter.allowNotify(
            context.getExceptionType(),
            context.getMethodSignature(),
            context.getExceptionMessage()
        );

        if (!allowed) {
            // 更新跳过次数
            int skipCount = rateLimiter.getSkipCount(
                context.getExceptionType(),
                context.getMethodSignature(),
                context.getExceptionMessage()
            );
            context.setRateLimited(true);
            context.setSkipCount(skipCount);
            log.debug("异常通知被限流 - 异常: {}, 方法: {}, 已跳过次数: {}",
                context.getExceptionType(), context.getMethodSignature(), skipCount);
        }

        return allowed;
    }

    /**
     * 构建邮件标题
     *
     * @param context 异常上下文
     * @return 邮件标题
     */
    private String buildEmailSubject(ExceptionContext context) {
        return String.format("[%s] %s - %s",
            context.getSeverity().getDesc(),
            context.getEnvironment() != null ? context.getEnvironment().toUpperCase() : "UNKNOWN",
            context.getExceptionType()
        );
    }

    /**
     * 构建邮件内容（HTML格式）
     *
     * @param context 异常上下文
     * @return HTML格式的邮件内容
     */
    private String buildEmailContent(ExceptionContext context) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append("h2 { color: #d9534f; border-bottom: 2px solid #d9534f; padding-bottom: 10px; }");
        html.append("h3 { color: #5bc0de; margin-top: 20px; border-left: 4px solid #5bc0de; padding-left: 10px; }");
        html.append(".section { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }");
        html.append(".label { font-weight: bold; color: #555; display: inline-block; width: 150px; }");
        html.append(".value { color: #333; }");
        html.append(".code { background: #272822; color: #f8f8f2; padding: 15px; border-radius: 5px; ");
        html.append("overflow-x: auto; font-family: 'Courier New', monospace; font-size: 12px; }");
        html.append(".warning { background: #fcf8e3; border-left: 4px solid #f0ad4e; padding: 10px; margin: 10px 0; }");
        html.append("</style></head><body>");

        // 标题
        html.append("<h2>").append(context.getSeverity().getDesc())
            .append(" 系统异常通知</h2>");

        // 限流提示
        if (Boolean.TRUE.equals(context.getRateLimited()) && context.getSkipCount() != null && context.getSkipCount() > 0) {
            html.append("<div class='warning'>");
            html.append("<strong>⚠️ 限流提示：</strong>此异常在过去10分钟内已跳过 ")
                .append(context.getSkipCount()).append(" 次邮件发送");
            html.append("</div>");
        }

        // 基础信息
        html.append("<div class='section'>");
        html.append("<h3>📊 基础信息</h3>");
        appendField(html, "异常类型", context.getExceptionType());
        appendField(html, "异常消息", escapeHtml(context.getExceptionMessage()));
        appendField(html, "发生时间", context.getOccurTime() != null ?
            context.getOccurTime().format(DATE_TIME_FORMATTER) : "N/A");
        appendField(html, "严重级别", context.getSeverity().getDesc());
        if (StringUtils.hasText(context.getDescription())) {
            appendField(html, "异常描述", escapeHtml(context.getDescription()));
        }
        html.append("</div>");

        // 方法信息
        html.append("<div class='section'>");
        html.append("<h3>📝 方法信息</h3>");
        appendField(html, "方法签名", escapeHtml(context.getMethodSignature()));
        appendField(html, "类名", context.getClassName());
        appendField(html, "方法名", context.getMethodName());
        if (StringUtils.hasText(context.getMethodArgs())) {
            appendField(html, "方法参数", escapeHtml(context.getMethodArgs()));
        }
        html.append("</div>");

        // 请求信息
        if (StringUtils.hasText(context.getRequestUrl())) {
            html.append("<div class='section'>");
            html.append("<h3>🌐 请求信息</h3>");
            appendField(html, "请求URL", escapeHtml(context.getRequestUrl()));
            appendField(html, "HTTP方法", context.getHttpMethod());
            if (StringUtils.hasText(context.getRequestParams())) {
                appendField(html, "请求参数", escapeHtml(context.getRequestParams()));
            }
            appendField(html, "TraceId", context.getTraceId());
            appendField(html, "用户ID", context.getUserId() != null ?
                context.getUserId().toString() : "未登录");
            appendField(html, "客户端IP", context.getClientIp());
            html.append("</div>");
        }

        // 系统信息
        html.append("<div class='section'>");
        html.append("<h3>💻 系统信息</h3>");
        appendField(html, "运行环境", context.getEnvironment());
        appendField(html, "服务器", context.getServerName());
        appendField(html, "应用名称", context.getApplicationName());
        if (context.getJvmUsedMemory() != null) {
            appendField(html, "JVM内存", String.format("%dMB / %dMB (已用/总共)",
                context.getJvmUsedMemory(), context.getJvmTotalMemory()));
        }
        if (context.getThreadCount() != null) {
            appendField(html, "线程数", context.getThreadCount().toString());
        }
        html.append("</div>");

        // 异常堆栈
        if (StringUtils.hasText(context.getStackTrace())) {
            html.append("<h3>📚 异常堆栈</h3>");
            html.append("<div class='code'>");
            html.append("<pre>").append(escapeHtml(context.getStackTrace())).append("</pre>");
            html.append("</div>");
        }

        // 页脚
        html.append("<hr style='margin-top: 30px;'>");
        html.append("<p style='color: #999; font-size: 12px;'>");
        html.append("此邮件由系统自动发送，请勿直接回复。<br>");
        html.append("如需查看详细日志，请使用 TraceId: <strong>")
            .append(context.getTraceId()).append("</strong> 进行检索。");
        html.append("</p>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * 在HTML中添加字段
     *
     * @param html  StringBuilder
     * @param label 标签
     * @param value 值
     */
    private void appendField(StringBuilder html, String label, String value) {
        if (value == null) {
            value = "N/A";
        }
        html.append("<div>");
        html.append("<span class='label'>").append(label).append(":</span>");
        html.append("<span class='value'>").append(value).append("</span>");
        html.append("</div>");
    }

    /**
     * HTML转义
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
