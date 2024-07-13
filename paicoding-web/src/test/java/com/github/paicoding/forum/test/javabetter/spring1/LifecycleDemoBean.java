package com.github.paicoding.forum.test.javabetter.spring1;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

public class LifecycleDemoBean implements InitializingBean, DisposableBean {

    // 使用@Value注解注入属性值，这里演示了如何从配置文件中读取值
    // 如果配置文件中没有定义lifecycle.demo.bean.name，则使用默认值"default name"
    @Value("${lifecycle.demo.bean.name:default name}")
    private String name;

    // 构造方法：在Bean实例化时调用
    public LifecycleDemoBean() {
        System.out.println("LifecycleDemoBean: 实例化");
    }

    // 属性赋值：Spring通过反射调用setter方法为Bean的属性注入值
    public void setName(String name) {
        System.out.println("LifecycleDemoBean: 属性赋值");
        this.name = name;
    }

    // 使用@PostConstruct注解的方法：在Bean的属性赋值完成后调用，用于执行初始化逻辑
    @PostConstruct
    public void postConstruct() {
        System.out.println("LifecycleDemoBean: @PostConstruct（初始化）");
    }

    // 实现InitializingBean接口：afterPropertiesSet方法在@PostConstruct注解的方法之后调用
    // 用于执行更多的初始化逻辑
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("LifecycleDemoBean: afterPropertiesSet（InitializingBean）");
    }

    // 自定义初始化方法：在XML配置或Java配置中指定，执行特定的初始化逻辑
    public void customInit() {
        System.out.println("LifecycleDemoBean: customInit（自定义初始化方法）");
    }

    // 使用@PreDestroy注解的方法：在容器销毁Bean之前调用，用于执行清理工作
    @PreDestroy
    public void preDestroy() {
        System.out.println("LifecycleDemoBean: @PreDestroy（销毁前）");
    }

    // 实现DisposableBean接口：destroy方法在@PreDestroy注解的方法之后调用
    // 用于执行清理资源等销毁逻辑
    @Override
    public void destroy() throws Exception {
        System.out.println("LifecycleDemoBean: destroy（DisposableBean）");
    }

    // 自定义销毁方法：在XML配置或Java配置中指定，执行特定的清理逻辑
    public void customDestroy() {
        System.out.println("LifecycleDemoBean: customDestroy（自定义销毁方法）");
    }
}
