package com.github.paicoding.forum.core.async;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 异步执行
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncExecute {
    /**
     * 是否开启异步执行
     *
     * @return
     */
    boolean value() default true;

    /**
     * 超时时间，默认3s
     *
     * @return
     */
    int timeOut() default 5;

    /**
     * 超时时间单位，默认秒，配合上面的 timeOut 使用
     *
     * @return
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 当出现超时返回的兜底逻辑,支持SpEL
     * 如果返回的是空字符串，则表示抛出异常
     *
     * @return
     */
    String timeOutRsp() default "";
}
