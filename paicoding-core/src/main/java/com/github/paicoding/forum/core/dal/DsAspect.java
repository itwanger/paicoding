package com.github.paicoding.forum.core.dal;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author YiHui
 * @date 2023/4/30
 */
@Aspect
public class DsAspect {
    /**
     * 切入点, 拦截类上、方法上有注解的方法，用于切换数据源
     */
    @Pointcut("@annotation(DB) || @within(DB)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        DB ds = method.getAnnotation(DB.class);
        if (ds == null) {
            // 获取类上的注解
            ds = (DB) proceedingJoinPoint.getSignature().getDeclaringType().getAnnotation(DB.class);
        }

        boolean notSet = DbContextHolder.get() != null;
        try {
            if (!notSet) {
                // 当上下文中没有时，则写入线程上下文，应该用哪个DB
                // 如果之前记录则不重新写入，避免出现覆盖
                DbContextHolder.set(ds == null ? null : ds.value().name());
            }
            return proceedingJoinPoint.proceed();
        } finally {
            // 清空上下文信息
            if (!notSet) {
                DbContextHolder.reset();
            }
        }
    }
}
