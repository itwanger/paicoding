package com.github.paicoding.forum.web.hook.aspect;

import com.github.paicoding.forum.web.global.GlobalInitService;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-06-26
 */

@Aspect
@Component
public class GlobalInfoResponseAspect {

    @Resource
    private GlobalInitService globalInitService;

    @Pointcut("execution(public com.github.paicoding.forum.web.global.vo.ResultVo com.github.paicoding.forum.web..*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object modifyGlobalResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed(); // 继续执行原方法

        if (result instanceof ResultVo) {
            ((ResultVo<?>) result).setGlobal(globalInitService.globalAttr());
        }

        return result;
    }
}
