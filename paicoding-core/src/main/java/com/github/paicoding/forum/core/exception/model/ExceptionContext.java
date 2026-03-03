package com.github.paicoding.forum.core.exception.model;

import com.github.paicoding.forum.api.model.enums.ExceptionSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 异常上下文对象
 * <p>
 * 用于封装异常发生时的完整上下文信息，包括异常信息、请求信息、系统信息等
 * </p>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    /**
     * 异常严重级别
     */
    private ExceptionSeverity severity;

    /**
     * 异常类型（完整类名）
     */
    private String exceptionType;

    /**
     * 异常消息
     */
    private String exceptionMessage;

    /**
     * 异常堆栈信息
     */
    private String stackTrace;

    /**
     * 异常发生时间
     */
    private LocalDateTime occurTime;

    /**
     * 异常描述（来自注解）
     */
    private String description;

    // ==================== 方法信息 ====================

    /**
     * 方法签名（类名.方法名）
     */
    private String methodSignature;

    /**
     * 方法参数（JSON格式）
     */
    private String methodArgs;

    /**
     * 方法所在类
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    // ==================== 请求信息 ====================

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * HTTP方法（GET、POST等）
     */
    private String httpMethod;

    /**
     * 请求参数（Query String）
     */
    private String requestParams;

    /**
     * 请求Body（JSON格式）
     */
    private String requestBody;

    /**
     * 请求头信息
     */
    private Map<String, String> requestHeaders;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * TraceId（链路追踪ID）
     */
    private String traceId;

    /**
     * 当前登录用户ID
     */
    private Long userId;

    /**
     * 当前登录用户名
     */
    private String userName;

    // ==================== 系统信息 ====================

    /**
     * 运行环境（dev、test、prod等）
     */
    private String environment;

    /**
     * 服务器名称/主机名
     */
    private String serverName;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * JVM最大内存（MB）
     */
    private Long jvmMaxMemory;

    /**
     * JVM总内存（MB）
     */
    private Long jvmTotalMemory;

    /**
     * JVM空闲内存（MB）
     */
    private Long jvmFreeMemory;

    /**
     * JVM已使用内存（MB）
     */
    private Long jvmUsedMemory;

    /**
     * 线程总数
     */
    private Integer threadCount;

    // ==================== 其他信息 ====================

    /**
     * 是否已限流
     */
    private Boolean rateLimited;

    /**
     * 限流窗口期内跳过的次数
     */
    private Integer skipCount;

    /**
     * 附加信息（自定义扩展字段）
     */
    private Map<String, Object> additionalInfo;
}
