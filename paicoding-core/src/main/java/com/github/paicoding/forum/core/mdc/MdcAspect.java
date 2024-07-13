package com.github.paicoding.forum.core.mdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Aspect
@Component
public class MdcAspect implements ApplicationContextAware {
    private ExpressionParser parser = new SpelExpressionParser();
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(MdcDot) || @within(MdcDot)")
    public void getLogAnnotation() {
    }

    @Around("getLogAnnotation()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        boolean hasTag = addMdcCode(joinPoint);
        try {
            Object ans = joinPoint.proceed();
            return ans;
        } finally {
            log.info("执行耗时: {}#{} = {}ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start);
            if (hasTag) {
                MdcUtil.reset();
            }
        }
    }

    private boolean addMdcCode(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MdcDot dot = method.getAnnotation(MdcDot.class);
        if (dot == null) {
            dot = (MdcDot) joinPoint.getSignature().getDeclaringType().getAnnotation(MdcDot.class);
        }

        if (dot != null) {
            MdcUtil.add("bizCode", loadBizCode(dot.bizCode(), joinPoint));
            return true;
        }
        return false;
    }

    private String loadBizCode(String key, ProceedingJoinPoint joinPoint) {
        if (StringUtils.isBlank(key)) {
            return "";
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        String[] params = parameterNameDiscoverer.getParameterNames(((MethodSignature) joinPoint.getSignature()).getMethod());
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            context.setVariable(params[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
