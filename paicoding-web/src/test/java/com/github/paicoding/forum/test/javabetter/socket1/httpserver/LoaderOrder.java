package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import java.lang.annotation.*;

/**
 * ServerLoader 对应的优先级
 * Created by @author yihui in 16:24 18/12/29.
 */
@Inherited
@Documented
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderOrder {
    /**
     * 数值越小，优先级越高；
     *
     * @return
     */
    int order() default 10;
}