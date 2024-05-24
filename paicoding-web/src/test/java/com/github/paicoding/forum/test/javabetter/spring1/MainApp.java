package com.github.paicoding.forum.test.javabetter.spring1;

import org.springframework.context.ApplicationContext;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/12/24
 */
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        // 使用 AppConfig 配置类初始化 ApplicationContext
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // 从 ApplicationContext 获取 messageService 的 bean
        MessageService service = context.getBean(MessageService.class);

        // 使用 bean
        service.printMessage();
    }
}

