package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import java.lang.annotation.*;

/**
 * Created by @author yihui in 19:52 18/12/30.
 */
@Inherited
@Documented
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EndPoint {

    /**
     * 优先级，数值越小优先级越高
     *
     * @return
     */
    int order() default 10;

    /**
     * 判断是否需要主动创建实例，true表示需要在加载时，创建一个实例; false表示不需要额外创建实例
     *
     * @return
     */
    boolean instance() default true;

}