package com.github.paicoding.forum.test.proxy;

import org.junit.Test;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class AsyncProxy {
    private static final AsyncProxy instance = new AsyncProxy();

    /**
     * 获取代理对象
     *
     * @param t   当前实体
     * @param <T> 泛型
     * @return 代理对象
     */
    public static <T> T proxy(T t) {
        Object target = t;
        while (AopUtils.isCglibProxy(target)) {
            target = AopProxyUtils.getSingletonTarget(target);
        }
        return instance.getProxy((T) target);
    }

    /**
     * 获取代理对象
     *
     * @param t   当前实体
     * @param <T> 泛型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(T t) {
        MethodInterceptor handler = new Handler(t);
        return (T) Enhancer.create(t.getClass(), handler);
    }

    /**
     * 代理类处理
     */
    class Handler implements MethodInterceptor {
        private final Object target;

        public Handler(Object target) {
            this.target = target;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            System.out.println("切面执行拦截器!!!");
            return methodProxy.invokeSuper(o, args);
        }
    }

    @Test
    public void testProxy() {
        DemoService demoService = new DemoService();
        String ans = AsyncProxy.proxy(demoService).showHello("这是一个测试!");
        System.out.println("response: " + ans);
    }


}
