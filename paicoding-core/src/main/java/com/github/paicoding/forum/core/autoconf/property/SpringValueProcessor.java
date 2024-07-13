package com.github.paicoding.forum.core.autoconf.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 配置变更注册, 找到 @Value 注解修饰的配置，注册到 SpringValueRegistry，实现统一的配置变更自动刷新管理
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Component
public class SpringValueProcessor implements BeanPostProcessor {
    private final PlaceholderHelper placeholderHelper;

    public SpringValueProcessor() {
        this.placeholderHelper = new PlaceholderHelper();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        for (Field field : findAllField(clazz)) {
            processField(bean, beanName, field);
        }
        for (Method method : findAllMethod(clazz)) {
            processMethod(bean, beanName, method);
        }
        return bean;
    }

    private List<Field> findAllField(Class clazz) {
        final List<Field> res = new LinkedList<>();
        ReflectionUtils.doWithFields(clazz, res::add);
        return res;
    }

    private List<Method> findAllMethod(Class clazz) {
        final List<Method> res = new LinkedList<>();
        ReflectionUtils.doWithMethods(clazz, res::add);
        return res;
    }

    /**
     * 成员变量上添加 @Value 方式绑定的配置
     *
     * @param bean
     * @param beanName
     * @param field
     */
    protected void processField(Object bean, String beanName, Field field) {
        // register @Value on field
        Value value = field.getAnnotation(Value.class);
        if (value == null) {
            return;
        }
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());

        if (keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            SpringValueRegistry.SpringValue springValue = new SpringValueRegistry.SpringValue(key, value.value(), bean, beanName, field);
            SpringValueRegistry.register(key, springValue);
            log.debug("Monitoring {}", springValue);
        }
    }

    /**
     * 通过 @Value 修饰方法的方式，通过一个传参进行实现的配置绑定
     *
     * @param bean
     * @param beanName
     * @param method
     */
    protected void processMethod(Object bean, String beanName, Method method) {
        //register @Value on method
        Value value = method.getAnnotation(Value.class);
        if (value == null) {
            return;
        }
        //skip Configuration bean methods
        if (method.getAnnotation(Bean.class) != null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            log.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters", bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
            return;
        }

        Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());

        if (keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            SpringValueRegistry.SpringValue springValue = new SpringValueRegistry.SpringValue(key, value.value(), bean, beanName, method);
            SpringValueRegistry.register(key, springValue);
            log.debug("Monitoring {}", springValue);
        }
    }
}
