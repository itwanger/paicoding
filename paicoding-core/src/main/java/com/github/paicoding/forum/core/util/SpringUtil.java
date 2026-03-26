package com.github.paicoding.forum.core.util;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author YiHui
 * @date 2022/8/29
 */
@Component
public class SpringUtil implements ApplicationContextAware, EnvironmentAware, ApplicationListener<ContextClosedEvent> {
    private volatile static ApplicationContext context;
    private volatile static Environment environment;

    private static Binder binder;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 容器启动时自动注入，方便后续获取bean
        SpringUtil.context = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringUtil.environment = environment;
        binder = Binder.get(environment);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (context == event.getApplicationContext()) {
            context = null;
            environment = null;
            binder = null;
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return
     */
    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * 获取bean
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static boolean isActive() {
        return context instanceof ConfigurableApplicationContext
                ? ((ConfigurableApplicationContext) context).isActive()
                : context != null;
    }

    public static <T> T getBean(Class<T> bean) {
        if (isActive()) {
            return context.getBean(bean);
        } else {
            throw new IllegalStateException("Spring ApplicationContext is not active or has been closed.");
        }
    }

    public static <T> T getBeanOrNull(Class<T> bean) {
        if (!isActive()) {
            return null;
        }
        try {
            return context.getBean(bean);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static Object getBeanOrNull(String beanName) {
        if (!isActive()) {
            return null;
        }
        try {
            return context.getBean(beanName);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasConfig(String key) {
        return environment != null && environment.containsProperty(key);
    }

    /**
     * 获取配置
     *
     * @param key
     * @return
     */
    public static String getConfig(String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    public static String getConfigOrElse(String mainKey, String slaveKey) {
        if (environment == null) {
            return null;
        }
        String ans = environment.getProperty(mainKey);
        if (ans == null) {
            return environment.getProperty(slaveKey);
        }
        return ans;
    }

    /**
     * 获取配置
     *
     * @param key
     * @param val 配置不存在时的默认值
     * @return
     */
    public static String getConfig(String key, String val) {
        return environment == null ? val : environment.getProperty(key, val);
    }

    /**
     * 发布事件消息
     *
     * @param event
     */
    public static void publishEvent(ApplicationEvent event) {
        if (isActive()) {
            context.publishEvent(event);
        }
    }


    /**
     * 配置绑定类
     *
     * @return
     */
    public static Binder getBinder() {
        return binder;
    }
}
