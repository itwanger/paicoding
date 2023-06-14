package com.github.paicoding.forum.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 记录注解
 *
 * @ClassName: RecordOperate
 * @Author: ygl
 * @Date: 2023/16/14 22:50
 * @Version: 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordOperate {

    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     * 这个是指业务类型，一般来说有：评论、回复、点赞、收藏、关注和系统等
     */
    String businessType() default "其它";


    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

    String desc() default "";

}
