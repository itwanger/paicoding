package com.github.paicoding.forum.core.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 异步执行
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Aspect
@Component
public class AsyncExecuteAspect implements ApplicationContextAware {

    /**
     * 超时执行的切面
     *
     * @param joinPoint
     * @param asyncExecute
     * @return
     * @throws Throwable
     */
    @Around("@annotation(asyncExecute)")
    public Object handle(ProceedingJoinPoint joinPoint, AsyncExecute asyncExecute) throws Throwable {
        if (!asyncExecute.value()) {
            // 不支持异步执行时，直接返回
            return joinPoint.proceed();
        }

        try {
            // 携带超时时间的执行调用
            return AsyncUtil.callWithTimeLimit(asyncExecute.timeOut(), asyncExecute.unit(), () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            if (StringUtils.isNotBlank(asyncExecute.timeOutRsp())) {
                return defaultRespWhenTimeOut(joinPoint, asyncExecute);
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private Object defaultRespWhenTimeOut(ProceedingJoinPoint joinPoint, AsyncExecute asyncExecute) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(this.applicationContext));

        // 超时，使用自定义的返回策略进行返回
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        log.info("{} 执行超时，返回兜底结果!", methodSignature.getMethod().getName());
        return parser.parseExpression(asyncExecute.timeOutRsp()).getValue(context);
    }


    private ExpressionParser parser;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.parser = new SpelExpressionParser();
        this.applicationContext = applicationContext;
    }
}
