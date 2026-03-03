package com.github.paicoding.forum.api.model.annotation;

import com.github.paicoding.forum.api.model.enums.ExceptionSeverity;

import java.lang.annotation.*;

/**
 * 异常邮件通知注解
 * <p>
 * 用于标记需要发送邮件通知的方法，当方法执行过程中抛出异常时，会自动发送邮件通知
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @ExceptionNotify(severity = ExceptionSeverity.CRITICAL)
 * public void criticalOperation() {
 *     // 关键业务操作
 * }
 *
 * @ExceptionNotify(
 *     severity = ExceptionSeverity.HIGH,
 *     notifyEmails = "admin@example.com",
 *     includeArgs = true
 * )
 * public void importantOperation(String param) {
 *     // 重要业务操作
 * }
 * }
 * </pre>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionNotify {

    /**
     * 异常严重级别，默认为 HIGH
     *
     * @return 异常严重级别
     */
    ExceptionSeverity severity() default ExceptionSeverity.HIGH;

    /**
     * 接收邮件的邮箱地址，多个邮箱用逗号分隔
     * <p>
     * 如果不指定，则使用配置文件中的默认邮箱地址
     * </p>
     *
     * @return 邮箱地址，空字符串表示使用默认配置
     */
    String notifyEmails() default "";

    /**
     * 是否在邮件中包含方法参数信息
     * <p>
     * 注意：如果参数中包含敏感信息（如密码、token等），请设置为 false
     * </p>
     *
     * @return true-包含参数信息，false-不包含
     */
    boolean includeArgs() default false;

    /**
     * 是否启用限流
     * <p>
     * 启用后，相同异常在限流窗口期内只会发送一次邮件
     * </p>
     *
     * @return true-启用限流，false-不限流
     */
    boolean enableRateLimit() default true;

    /**
     * 异常描述，用于在邮件中补充说明该异常的业务含义
     *
     * @return 异常描述
     */
    String description() default "";
}
