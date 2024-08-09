package com.github.paicoding.forum.core.async;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 异步执行
 *
 * @author YiHui
 * @date 2023/11/10
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
     * true 表示后台执行
     * false 同步执行
     *
     * @return
     */
    boolean backRun() default false;

    /**
     * 超时时间，默认3s
     *
     * @return
     */
    int timeOut() default 3;

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
