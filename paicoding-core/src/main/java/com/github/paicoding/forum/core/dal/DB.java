package com.github.paicoding.forum.core.dal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YiHui
 * @date 2023/4/30
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DB {
    /**
     * 启用的数据源，默认主库
     *
     * @return
     */
    DbEnum value() default DbEnum.MASTER;

    /**
     * 启用的数据源
     *
     * @return
     */
    String ds() default "";
}
